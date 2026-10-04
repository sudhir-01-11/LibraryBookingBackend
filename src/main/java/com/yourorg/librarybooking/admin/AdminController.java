package com.yourorg.librarybooking.admin;

import com.yourorg.librarybooking.booking.Booking;
import com.yourorg.librarybooking.booking.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final BookingRepository bookingRepository;

    public AdminController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<AdminBookingResponse> response = bookings.stream()
                .map(b -> new AdminBookingResponse(
                        b.getId(),
                        b.getUserId(),
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

    public record AdminBookingResponse(Long bookingId, Long userId, Long zoneId, Long seatId, ZonedDateTime startTime, ZonedDateTime endTime, String status, java.math.BigDecimal fare) {}
}
