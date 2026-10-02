package com.yourorg.librarybooking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT s.seat_id FROM seats s " +
                   "WHERE s.zone_id = :zoneId AND s.is_functional = true " +
                   "AND NOT EXISTS (" +
                   "  SELECT 1 FROM bookings b " +
                   "  WHERE b.seat_id = s.seat_id " +
                   "  AND b.status IN ('HELD', 'CONFIRMED') " +
                   "  AND b.time_range && tstzrange(cast(:startTime as timestamptz), cast(:endTime as timestamptz), '[)') " +
                   ")", nativeQuery = true)
    List<Long> findAvailableSeats(
            @Param("zoneId") Long zoneId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );
}
