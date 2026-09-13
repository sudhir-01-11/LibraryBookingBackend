CREATE TABLE zones (
    zone_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zone_name VARCHAR(100) NOT NULL,
    total_seats INTEGER NOT NULL DEFAULT 0,
    hourly_rate NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_zones_name UNIQUE (zone_name),
    CONSTRAINT chk_zones_total_seats CHECK (total_seats >= 0),
    CONSTRAINT chk_zones_hourly_rate CHECK (hourly_rate > 0)
);

CREATE TABLE seats (
    seat_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zone_id BIGINT NOT NULL,
    is_functional BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seats_zone FOREIGN KEY (zone_id) REFERENCES zones (zone_id) ON DELETE RESTRICT,
    CONSTRAINT uq_seats_id_zone UNIQUE (seat_id, zone_id)
);
