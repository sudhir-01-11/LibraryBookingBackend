package com.yourorg.librarybooking.waitlist;

import io.hypersistence.utils.hibernate.type.range.Range;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class WaitlistService {

    private final WaitingListRepository waitingListRepository;

    public WaitlistService(WaitingListRepository waitingListRepository) {
        this.waitingListRepository = waitingListRepository;
    }

    @Transactional
    public WaitingList joinWaitlist(Long userId, Long zoneId, ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime.isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past.");
        }
        
        int activeCount = waitingListRepository.countActiveWaitlistInTimeRange(zoneId, startTime, endTime);
        if (activeCount >= 10) {
            throw new IllegalStateException("Waitlist is currently full (maximum 10 people).");
        }

        WaitingList waitlist = new WaitingList();
        waitlist.setUserId(userId);
        waitlist.setZoneId(zoneId);
        waitlist.setRequestedTimeRange(Range.zonedDateTimeRange(
                "[" + startTime.toString() + "," + endTime.toString() + ")"
        ));
        waitlist.setStatus(WaitingList.Status.PENDING_PAYMENT);

        return waitingListRepository.save(waitlist);
    }

    public List<WaitingList> getUserWaitlists(Long userId) {
        return waitingListRepository.findByUserId(userId);
    }
}
