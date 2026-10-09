package com.yourorg.librarybooking.booking;

import com.yourorg.librarybooking.common.exception.SeatUnavailableException;
import io.hypersistence.utils.hibernate.type.range.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import com.yourorg.librarybooking.payment.PaymentService;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    @Value("${application.booking.hold-duration-minutes:5}")
    private int holdDurationMinutes;

    public BookingService(BookingRepository bookingRepository, PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.paymentService = paymentService;
    }

    @Transactional(readOnly = true)
    public List<Long> getAvailableSeats(Long zoneId, ZonedDateTime startTime, ZonedDateTime endTime) {
        validateTimeRange(startTime, endTime);
        return bookingRepository.findAvailableSeats(zoneId, startTime, endTime);
    }

    @Transactional
    public Booking holdSeat(Long userId, Long zoneId, Long seatId, ZonedDateTime startTime, ZonedDateTime endTime) {
        validateTimeRange(startTime, endTime);
        
        // Ensure seat is available before trying to insert
        List<Long> availableSeats = bookingRepository.findAvailableSeats(zoneId, startTime, endTime);
        boolean seatFound = false;
        // Handle Hibernate native query returning BigInteger or Long due to type erasure
        for (Object obj : availableSeats) {
            if (obj instanceof Number && ((Number) obj).longValue() == seatId.longValue()) {
                seatFound = true;
                break;
            }
        }
        if (!seatFound) {
            throw new SeatUnavailableException("Seat " + seatId + " is not available for the selected time.");
        }

        try {
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setZoneId(zoneId);
            booking.setSeatId(seatId);
            
            // TSTZRANGE uses lower inclusive, upper exclusive `[)`
            booking.setTimeRange(Range.zonedDateTimeRange(
                    "[" + startTime.toString() + "," + endTime.toString() + ")"
            ));
            
            booking.setStatus(Booking.BookingStatus.HELD);
            booking.setBookingSource(Booking.BookingSource.DIRECT);
            booking.setHoldExpiresAt(ZonedDateTime.now().plusMinutes(holdDurationMinutes));
            
            // Calculate fare (mock logic, ideally fetched from zone.getHourlyRate())
            long hours = Duration.between(startTime, endTime).toHours();
            if (hours == 0) hours = 1; // min 1 hour charge for example
            booking.setFareSnapshot(BigDecimal.valueOf(hours * 10)); // Example rate: 10 per hour

            return bookingRepository.save(booking);

        } catch (DataIntegrityViolationException ex) {
            ex.printStackTrace();
            // This catches the Postgres `EXCLUDE USING GIST` constraint on race conditions
            throw new SeatUnavailableException("Race condition: " + ex.getMessage());
        }
    }

    private void validateTimeRange(ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime.isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past.");
        }
        
        Duration duration = Duration.between(startTime, endTime);
        if (duration.toMinutes() < 30) {
            throw new IllegalArgumentException("Minimum booking duration is 30 minutes.");
        }
        if (duration.toMinutes() > 180) {
            throw new IllegalArgumentException("Maximum booking duration is 3 hours.");
        }
    }
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to cancel this booking.");
        }

        if (booking.getTimeRange().lower().isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel a booking that has already started.");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled.");
        }

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            try {
                paymentService.processRefund(bookingId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
            }
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}
