package com.yourorg.librarybooking.config;

import com.yourorg.librarybooking.seat.Seat;
import com.yourorg.librarybooking.seat.SeatRepository;
import com.yourorg.librarybooking.zone.Zone;
import com.yourorg.librarybooking.zone.ZoneRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ZoneRepository zoneRepository, SeatRepository seatRepository) {
        return args -> {
            if (zoneRepository.count() == 0) {
                Zone quietZone = new Zone();
                quietZone.setName("Quiet Zone");
                quietZone.setDescription("Perfect for deep focus and silent studying.");
                quietZone.setCapacity(3);
                quietZone.setPricePerHour(new BigDecimal("20.00"));
                quietZone.setImageUrl("https://images.unsplash.com/photo-1568667256549-094345857637?auto=format&fit=crop&q=80&w=800");
                quietZone = zoneRepository.save(quietZone);

                for (int i = 1; i <= 3; i++) {
                    Seat seat = new Seat();
                    seat.setSeatNumber(String.valueOf(i));
                    seat.setZone(quietZone);
                    seat.setActive(true);
                    seatRepository.save(seat);
                }

                Zone discussionZone = new Zone();
                discussionZone.setName("Discussion Zone");
                discussionZone.setDescription("Collaborative space for group projects.");
                discussionZone.setCapacity(4);
                discussionZone.setPricePerHour(new BigDecimal("35.00"));
                discussionZone.setImageUrl("https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&q=80&w=800");
                discussionZone = zoneRepository.save(discussionZone);

                for (int i = 1; i <= 4; i++) {
                    Seat seat = new Seat();
                    seat.setSeatNumber("D" + i);
                    seat.setZone(discussionZone);
                    seat.setActive(true);
                    seatRepository.save(seat);
                }
            }
        };
    }
}
