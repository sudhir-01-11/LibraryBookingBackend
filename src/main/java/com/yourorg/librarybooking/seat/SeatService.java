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
        return seatRepository.findByZoneIdAndIsFunctionalTrue(zoneId).stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        String.valueOf(seat.getId()), // Since there's no seat_number, we use ID as string
                        seat.getZone().getId(),
                        seat.isFunctional()
                ))
                .collect(Collectors.toList());
    }
}
