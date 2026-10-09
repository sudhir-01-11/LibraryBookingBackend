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
                        "Standard " + zone.getName() + " for studying.", // Fallback description
                        zone.getCapacity(),
                        zone.getPricePerHour(),
                        "https://images.unsplash.com/photo-1568667256549-094345857637?auto=format&fit=crop&q=80&w=800" // Fallback image
                ))
                .collect(Collectors.toList());
    }
}
