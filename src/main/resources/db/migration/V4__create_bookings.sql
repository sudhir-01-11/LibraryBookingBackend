CREATE TABLE bookings (
    booking_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    zone_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    time_range TSTZRANGE NOT NULL,
    status booking_status NOT NULL DEFAULT 'HELD',
    booking_source booking_source NOT NULL DEFAULT 'DIRECT',
    fare_snapshot NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hold_expires_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_zone FOREIGN KEY (zone_id) REFERENCES zones (zone_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_seat_in_zone FOREIGN KEY (seat_id, zone_id) REFERENCES seats (seat_id, zone_id) ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_time_range_not_empty CHECK (NOT isempty(time_range)),
    CONSTRAINT chk_bookings_half_open_range CHECK (lower_inc(time_range) AND NOT upper_inc(time_range)),
    CONSTRAINT chk_bookings_duration CHECK (
        upper(time_range) - lower(time_range) >= INTERVAL '30 minutes'
        AND upper(time_range) - lower(time_range) <= INTERVAL '3 hours'
    ),
    CONSTRAINT chk_bookings_fare_snapshot CHECK (fare_snapshot > 0),
    CONSTRAINT chk_bookings_hold_expiry CHECK (
        (status = 'HELD' AND hold_expires_at IS NOT NULL)
        OR (status <> 'HELD')
    )
);
