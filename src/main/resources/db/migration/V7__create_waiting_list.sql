CREATE TABLE waiting_list (
    waiting_list_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    zone_id BIGINT NOT NULL,
    requested_time_range TSTZRANGE NOT NULL,
    payment_id BIGINT,
    status waiting_list_status NOT NULL DEFAULT 'PENDING_PAYMENT',
    queued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    refunded_at TIMESTAMPTZ,
    CONSTRAINT fk_waiting_list_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_waiting_list_zone FOREIGN KEY (zone_id) REFERENCES zones (zone_id) ON DELETE RESTRICT,
    CONSTRAINT fk_waiting_list_payment FOREIGN KEY (payment_id) REFERENCES payments (payment_id) ON DELETE RESTRICT,
    CONSTRAINT uq_waiting_list_payment UNIQUE (payment_id),
    CONSTRAINT chk_waiting_list_time_range_not_empty CHECK (NOT isempty(requested_time_range)),
    CONSTRAINT chk_waiting_list_half_open_range CHECK (lower_inc(requested_time_range) AND NOT upper_inc(requested_time_range)),
    CONSTRAINT chk_waiting_list_duration CHECK (
        upper(requested_time_range) - lower(requested_time_range) >= INTERVAL '30 minutes'
        AND upper(requested_time_range) - lower(requested_time_range) <= INTERVAL '3 hours'
    )
);
