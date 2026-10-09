package com.yourorg.librarybooking.zone;

import com.yourorg.librarybooking.zone.dto.ZoneResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public List<ZoneResponse> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(zone -> new ZoneResponse(
                        zone.getId(),
                        zone.getName(),
                        zone.getDescription(),
                        zone.getCapacity(),
                        zone.getPricePerHour(),
                        zone.getImageUrl()
                ))
                .collect(Collectors.toList());
    }
}
