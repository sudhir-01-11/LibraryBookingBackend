package com.yourorg.librarybooking.waitlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {
    
    @Query(value = "SELECT count(*) FROM waiting_list w " +
                   "WHERE w.zone_id = :zoneId " +
                   "AND w.status = 'ACTIVE' " +
                   "AND w.requested_time_range && tstzrange(cast(:startTime as timestamptz), cast(:endTime as timestamptz), '[)')", 
           nativeQuery = true)
    int countActiveWaitlistInTimeRange(
            @Param("zoneId") Long zoneId, 
            @Param("startTime") ZonedDateTime startTime, 
            @Param("endTime") ZonedDateTime endTime
    );

    @Query(value = "SELECT count(*) FROM waiting_list w " +
                   "WHERE w.zone_id = :zoneId " +
                   "AND w.status = 'ACTIVE' " +
                   "AND w.requested_time_range && tstzrange(cast(:startTime as timestamptz), cast(:endTime as timestamptz), '[)') " +
                   "AND w.queued_at <= cast(:queuedAt as timestamptz)", 
           nativeQuery = true)
    int getQueuePosition(
            @Param("zoneId") Long zoneId, 
            @Param("startTime") ZonedDateTime startTime, 
            @Param("endTime") ZonedDateTime endTime,
            @Param("queuedAt") ZonedDateTime queuedAt
    );

    List<WaitingList> findByUserId(Long userId);
    
    java.util.Optional<WaitingList> findByPaymentId(Long paymentId);

    @Query(value = "SELECT * FROM waiting_list w " +
                   "WHERE w.status = 'ACTIVE' " +
                   "AND lower(w.requested_time_range) <= cast(:time as timestamptz)", 
           nativeQuery = true)
    List<WaitingList> findAllActiveStartingBefore(@Param("time") ZonedDateTime time);

    @Query(value = "SELECT * FROM waiting_list w " +
                   "WHERE w.status = 'ACTIVE' " +
                   "ORDER BY w.queued_at ASC", nativeQuery = true)
    List<WaitingList> findAllActiveOrderedByTime();
}
