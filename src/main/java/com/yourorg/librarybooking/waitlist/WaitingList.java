package com.yourorg.librarybooking.waitlist;

import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.ZonedDateTime;

@Entity
@Table(name = "waiting_list")
public class WaitingList {

    public enum Status {
        PENDING_PAYMENT, ACTIVE, ASSIGNED, CANCELLED, EXPIRED, REFUND_PENDING, REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiting_list_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Type(PostgreSQLRangeType.class)
    @Column(name = "requested_time_range", columnDefinition = "tstzrange", nullable = false)
    private Range<ZonedDateTime> requestedTimeRange;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING_PAYMENT;

    @Column(name = "queued_at", nullable = false, updatable = false)
    private ZonedDateTime queuedAt = ZonedDateTime.now();

    @Column(name = "assigned_at")
    private ZonedDateTime assignedAt;

    @Column(name = "cancelled_at")
    private ZonedDateTime cancelledAt;

    @Column(name = "refunded_at")
    private ZonedDateTime refundedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public Range<ZonedDateTime> getRequestedTimeRange() { return requestedTimeRange; }
    public void setRequestedTimeRange(Range<ZonedDateTime> requestedTimeRange) { this.requestedTimeRange = requestedTimeRange; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public ZonedDateTime getQueuedAt() { return queuedAt; }
    public void setQueuedAt(ZonedDateTime queuedAt) { this.queuedAt = queuedAt; }

    public ZonedDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(ZonedDateTime assignedAt) { this.assignedAt = assignedAt; }

    public ZonedDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(ZonedDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public ZonedDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(ZonedDateTime refundedAt) { this.refundedAt = refundedAt; }
}
