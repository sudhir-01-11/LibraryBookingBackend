package com.yourorg.librarybooking.waitlist;

import com.yourorg.librarybooking.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping("/join")
    public ResponseEntity<WaitlistResponse> joinWaitlist(
            @AuthenticationPrincipal User user,
            @RequestBody JoinWaitlistRequest request) {
        
        WaitingList waitlist = waitlistService.joinWaitlist(
                user.getId(), 
                request.zoneId(), 
                request.startTime(), 
                request.endTime()
        );

        long hours = java.time.Duration.between(request.startTime(), request.endTime()).toHours();
        if (hours == 0) hours = 1;
        java.math.BigDecimal fare = java.math.BigDecimal.valueOf(hours * 10);

        return ResponseEntity.ok(new WaitlistResponse(
                waitlist.getId(),
                waitlist.getZoneId(),
                waitlist.getRequestedTimeRange().lower(),
                waitlist.getRequestedTimeRange().upper(),
                waitlist.getStatus().name(),
                fare
        ));
    }

    @GetMapping("/my-waitlists")
    public ResponseEntity<List<WaitlistResponse>> getMyWaitlists(@AuthenticationPrincipal User user) {
        List<WaitlistResponse> response = waitlistService.getUserWaitlists(user.getId())
                .stream()
                .map(w -> {
                    long hours = java.time.Duration.between(w.getRequestedTimeRange().lower(), w.getRequestedTimeRange().upper()).toHours();
                    if (hours == 0) hours = 1;
                    return new WaitlistResponse(
                            w.getId(),
                            w.getZoneId(),
                            w.getRequestedTimeRange().lower(),
                            w.getRequestedTimeRange().upper(),
                            w.getStatus().name(),
                            java.math.BigDecimal.valueOf(hours * 10)
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    public record JoinWaitlistRequest(Long zoneId, ZonedDateTime startTime, ZonedDateTime endTime) {}
    public record WaitlistResponse(Long waitlistId, Long zoneId, ZonedDateTime startTime, ZonedDateTime endTime, String status, java.math.BigDecimal fare) {}
}
