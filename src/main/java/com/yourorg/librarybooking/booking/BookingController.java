package com.yourorg.librarybooking.booking;

import com.yourorg.librarybooking.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam Long zoneId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startTime,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endTime) {
        
        List<Long> availableSeatIds = bookingService.getAvailableSeats(zoneId, startTime, endTime);
        return ResponseEntity.ok(new AvailabilityResponse(zoneId, startTime, endTime, availableSeatIds));
    }

    @PostMapping("/hold")
    public ResponseEntity<BookingResponse> holdSeat(
            @AuthenticationPrincipal User user,
            @RequestBody BookingRequest request) {
        
        Long userId = user.getId(); 
        Booking booking = bookingService.holdSeat(
                userId,
                request.zoneId(),
                request.seatId(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity.ok(new BookingResponse(
                booking.getId(),
                booking.getSeatId(),
                booking.getStatus().name(),
                booking.getFareSnapshot(),
                booking.getHoldExpiresAt()
        ));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<MyBookingResponse>> getMyBookings(@AuthenticationPrincipal User user) {
        List<Booking> bookings = bookingService.getUserBookings(user.getId());
        List<MyBookingResponse> response = bookings.stream()
                .map(b -> new MyBookingResponse(
                        b.getId(),
                        b.getZoneId(),
                        b.getSeatId(),
                        b.getTimeRange().lower(),
                        b.getTimeRange().upper(),
                        b.getStatus().name(),
                        b.getFareSnapshot()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<MyBookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Booking booking = bookingService.cancelBooking(id, user.getId());
        return ResponseEntity.ok(new MyBookingResponse(
                booking.getId(),
                booking.getZoneId(),
                booking.getSeatId(),
                booking.getTimeRange().lower(),
                booking.getTimeRange().upper(),
                booking.getStatus().name(),
                booking.getFareSnapshot()
        ));
    }

    // Record DTOs mapped into the controller file for brevity (alternatively put in dto package)
    public record BookingRequest(Long zoneId, Long seatId, ZonedDateTime startTime, ZonedDateTime endTime) {}
    public record BookingResponse(Long bookingId, Long seatId, String status, java.math.BigDecimal fare, ZonedDateTime holdExpiresAt) {}
    public record MyBookingResponse(Long bookingId, Long zoneId, Long seatId, ZonedDateTime startTime, ZonedDateTime endTime, String status, java.math.BigDecimal fare) {}
    public record AvailabilityResponse(Long zoneId, ZonedDateTime startTime, ZonedDateTime endTime, List<Long> availableSeatIds) {}
}
