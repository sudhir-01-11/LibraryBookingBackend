package com.yourorg.librarybooking.booking;

import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Type(PostgreSQLRangeType.class)
    @Column(name = "time_range", columnDefinition = "tstzrange", nullable = false)
    private Range<ZonedDateTime> timeRange;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.HELD;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "booking_source", nullable = false)
    private BookingSource bookingSource = BookingSource.DIRECT;

    @Column(name = "fare_snapshot", nullable = false)
    private BigDecimal fareSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "hold_expires_at")
    private ZonedDateTime holdExpiresAt;

    @Column(name = "cancelled_at")
    private ZonedDateTime cancelledAt;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    public enum BookingStatus {
        HELD, CONFIRMED, CANCELLED, EXPIRED, COMPLETED
    }

    public enum BookingSource {
        DIRECT, WAITLIST
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public Range<ZonedDateTime> getTimeRange() { return timeRange; }
    public void setTimeRange(Range<ZonedDateTime> timeRange) { this.timeRange = timeRange; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public BookingSource getBookingSource() { return bookingSource; }
    public void setBookingSource(BookingSource bookingSource) { this.bookingSource = bookingSource; }

    public BigDecimal getFareSnapshot() { return fareSnapshot; }
    public void setFareSnapshot(BigDecimal fareSnapshot) { this.fareSnapshot = fareSnapshot; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(ZonedDateTime holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }

    public ZonedDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(ZonedDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public ZonedDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; }
}
