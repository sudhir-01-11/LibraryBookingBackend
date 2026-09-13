CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE membership_status AS ENUM ('ACTIVE', 'EXPIRED', 'SUSPENDED');
CREATE TYPE booking_status AS ENUM ('HELD', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'COMPLETED');
CREATE TYPE booking_source AS ENUM ('DIRECT', 'WAITLIST');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUND_PENDING', 'PARTIALLY_REFUNDED', 'REFUNDED');
CREATE TYPE waiting_list_status AS ENUM ('PENDING_PAYMENT', 'ACTIVE', 'ASSIGNED', 'CANCELLED', 'EXPIRED', 'REFUND_PENDING', 'REFUNDED');

CREATE TABLE users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    fine_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    membership_status membership_status NOT NULL DEFAULT 'ACTIVE',
    membership_expiry_date DATE,
    role user_role NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone),
    CONSTRAINT chk_users_fine_amount CHECK (fine_amount >= 0)
);

CREATE TABLE library_settings (
    setting_id SMALLINT PRIMARY KEY,
    opening_time TIME NOT NULL DEFAULT TIME '08:00',
    closing_time TIME NOT NULL DEFAULT TIME '00:00',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    updated_by BIGINT REFERENCES users (user_id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_library_settings_singleton CHECK (setting_id = 1),
    CONSTRAINT chk_library_settings_timezone CHECK (timezone = 'Asia/Kolkata')
);
