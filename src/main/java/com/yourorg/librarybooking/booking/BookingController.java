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
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam Long zoneId,
            @RequestParam ZonedDateTime startTime,
            @RequestParam ZonedDateTime endTime) {
        
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

    // Record DTOs mapped into the controller file for brevity (alternatively put in dto package)
    public record BookingRequest(Long zoneId, Long seatId, ZonedDateTime startTime, ZonedDateTime endTime) {}
    public record BookingResponse(Long bookingId, Long seatId, String status, java.math.BigDecimal fare, ZonedDateTime holdExpiresAt) {}
    public record AvailabilityResponse(Long zoneId, ZonedDateTime startTime, ZonedDateTime endTime, List<Long> availableSeatIds) {}
}
