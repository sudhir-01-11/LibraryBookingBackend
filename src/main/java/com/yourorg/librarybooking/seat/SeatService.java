package com.yourorg.librarybooking.seat;

import com.yourorg.librarybooking.seat.dto.SeatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<SeatResponse> getSeatsByZone(Long zoneId) {
        return seatRepository.findByZoneIdAndIsActiveTrue(zoneId).stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        seat.getZone().getId(),
                        seat.isActive()
                ))
                .collect(Collectors.toList());
    }
}
