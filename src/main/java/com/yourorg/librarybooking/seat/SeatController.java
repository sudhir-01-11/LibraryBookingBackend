package com.yourorg.librarybooking.seat;

import com.yourorg.librarybooking.seat.dto.SeatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones/{zoneId}/seats")
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(seatService.getSeatsByZone(zoneId));
    }
}
