CREATE INDEX idx_seats_available_by_zone
    ON seats (zone_id, seat_id)
    WHERE is_functional = TRUE;

CREATE INDEX idx_bookings_zone_time_range
    ON bookings USING GIST (zone_id, time_range)
    WHERE status IN ('HELD', 'CONFIRMED');

CREATE INDEX idx_bookings_user_status
    ON bookings (user_id, status);

CREATE INDEX idx_payments_booking_id
    ON payments (booking_id);

CREATE INDEX idx_waiting_list_promotion
    ON waiting_list (zone_id, queued_at, waiting_list_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_waiting_list_zone_time_range
    ON waiting_list USING GIST (zone_id, requested_time_range)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uq_active_waiting_list_request
    ON waiting_list (user_id, zone_id, requested_time_range)
    WHERE status IN ('PENDING_PAYMENT', 'ACTIVE');

ALTER TABLE waiting_list
    ADD CONSTRAINT ex_waiting_list_active_user_time_range
    EXCLUDE USING GIST (
        user_id WITH =,
        requested_time_range WITH &&
    )
    WHERE (status IN ('PENDING_PAYMENT', 'ACTIVE'));
