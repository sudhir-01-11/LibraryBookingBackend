ALTER TABLE bookings
    ADD CONSTRAINT ex_bookings_active_seat_time_range
    EXCLUDE USING GIST (
        seat_id WITH =,
        time_range WITH &&
    )
    WHERE (status IN ('HELD', 'CONFIRMED'));

ALTER TABLE bookings
    ADD CONSTRAINT ex_bookings_active_user_time_range
    EXCLUDE USING GIST (
        user_id WITH =,
        time_range WITH &&
    )
    WHERE (status IN ('HELD', 'CONFIRMED'));
