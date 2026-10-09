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
                quietZone.setCapacity(3);
                quietZone.setPricePerHour(new BigDecimal("20.00"));
                quietZone = zoneRepository.save(quietZone);

                for (int i = 1; i <= 3; i++) {
                    Seat seat = new Seat();
                    seat.setZone(quietZone);
                    seat.setFunctional(true);
                    seatRepository.save(seat);
                }

                Zone discussionZone = new Zone();
                discussionZone.setName("Discussion Zone");
                discussionZone.setCapacity(4);
                discussionZone.setPricePerHour(new BigDecimal("35.00"));
                discussionZone = zoneRepository.save(discussionZone);

                for (int i = 1; i <= 4; i++) {
                    Seat seat = new Seat();
                    seat.setZone(discussionZone);
                    seat.setFunctional(true);
                    seatRepository.save(seat);
                }
            }
        };
    }
}
