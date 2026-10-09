package com.yourorg.librarybooking.waitlist;

import com.yourorg.librarybooking.booking.Booking;
import com.yourorg.librarybooking.booking.BookingService;
import com.yourorg.librarybooking.user.User;
import com.yourorg.librarybooking.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class WaitlistPromotionJob {

    private final WaitingListRepository waitingListRepository;
    private final BookingService bookingService;
    private final com.yourorg.librarybooking.booking.BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final com.yourorg.librarybooking.payment.PaymentRepository paymentRepository;
    private final com.yourorg.librarybooking.payment.PaymentService paymentService;

    public WaitlistPromotionJob(WaitingListRepository waitingListRepository, BookingService bookingService, 
                                com.yourorg.librarybooking.booking.BookingRepository bookingRepository,
                                UserRepository userRepository, 
                                com.yourorg.librarybooking.payment.PaymentRepository paymentRepository, 
                                com.yourorg.librarybooking.payment.PaymentService paymentService) {
        this.waitingListRepository = waitingListRepository;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedRate = 60000)
    public void promoteWaitlistedUsers() {
        System.out.println("Running Waitlist Promotion & Refund Job...");
        
        // 1. Handle auto-refunds for waitlists that are less than 5 mins away
        ZonedDateTime fiveMinsFromNow = ZonedDateTime.now().plusMinutes(5);
        List<WaitingList> toRefund = waitingListRepository.findAllActiveStartingBefore(fiveMinsFromNow);
        
        for (WaitingList waitlist : toRefund) {
            try {
                if (waitlist.getPaymentId() != null) {
                    System.out.println("Waitlist " + waitlist.getId() + " is within 5 mins. Refunding...");
                    
                    // We must find the payment and temporarily assign it to a fake booking or use a new method?
                    // PaymentService processRefund expects a bookingId. Let's create an overloaded method for waitlist
                    // Or we just fetch the payment and use Razorpay client directly here?
                    // Let's use the PaymentService directly
                    paymentService.processWaitlistRefund(waitlist);
                }
                waitlist.setStatus(WaitingList.Status.EXPIRED);
                waitingListRepository.save(waitlist);
            } catch (Exception e) {
                System.err.println("Error refunding waitlist: " + e.getMessage());
            }
        }
        
        // 2. Handle Promotions
        List<WaitingList> activeWaitlists = waitingListRepository.findAllActiveOrderedByTime();
        for (WaitingList waitlist : activeWaitlists) {
            ZonedDateTime start = waitlist.getRequestedTimeRange().lower();
            ZonedDateTime end = waitlist.getRequestedTimeRange().upper();

            try {
                List<Long> availableSeats = bookingService.getAvailableSeats(waitlist.getZoneId(), start, end);
                
                if (!availableSeats.isEmpty()) {
                    Long seatId = availableSeats.get(0);
                    
                    // Since they already paid, hold the seat then confirm it
                    Booking booking = bookingService.holdSeat(
                            waitlist.getUserId(), 
                            waitlist.getZoneId(), 
                            seatId, 
                            start, 
                            end
                    );
                    
                    booking.setStatus(Booking.BookingStatus.CONFIRMED);
                    booking.setBookingSource(Booking.BookingSource.WAITLIST);
                    booking = bookingRepository.save(booking);
                    
                    // Link payment to this new booking
                    if (waitlist.getPaymentId() != null) {
                        Booking finalBooking = booking;
                        paymentRepository.findById(waitlist.getPaymentId()).ifPresent(p -> {
                            p.setBooking(finalBooking);
                            paymentRepository.save(p);
                        });
                    }
                    
                    waitlist.setStatus(WaitingList.Status.ASSIGNED);
                    waitlist.setAssignedAt(ZonedDateTime.now());
                    waitingListRepository.save(waitlist);
                }
            } catch (Exception e) {
                System.err.println("Error processing waitlist promotion for ID: " + waitlist.getId() + " - " + e.getMessage());
            }
        }
    }
}
