
CREATE DATABASE IF NOT EXISTS ticket_booking_db;

USE ticket_booking_db;

-- 1. Trains
CREATE TABLE IF NOT EXISTS trains (
    train_id INT PRIMARY KEY,
    train_name VARCHAR(100) NOT NULL,
    source_station VARCHAR(100),
    destination_station VARCHAR(100),
    total_seats INT NOT NULL,
    CONSTRAINT chk_total_seats CHECK (total_seats > 0)
) ENGINE = InnoDB;

-- 2. Seats belonging to a train
CREATE TABLE IF NOT EXISTS seats (
    seat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    seat_number INT NOT NULL,
    seat_status ENUM('AVAILABLE', 'BOOKED')
        NOT NULL DEFAULT 'AVAILABLE',

    CONSTRAINT fk_seat_train
        FOREIGN KEY (train_id)
        REFERENCES trains(train_id),

    CONSTRAINT uq_train_seat
        UNIQUE (train_id, seat_number),

    CONSTRAINT chk_seat_number
        CHECK (seat_number > 0)
) ENGINE = InnoDB;

-- 3. Passengers
CREATE TABLE IF NOT EXISTS passengers (
    passenger_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    passenger_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE
) ENGINE = InnoDB;

-- 4. Bookings
CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    passenger_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    booking_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_status ENUM('CONFIRMED', 'CANCELLED')
        NOT NULL DEFAULT 'CONFIRMED',

    -- A seat can have only one active booking.
    -- Cancelled bookings have NULL here and can be retained
    -- as history while allowing the seat to be booked again.
    active_seat_id BIGINT GENERATED ALWAYS AS (
        CASE
            WHEN booking_status = 'CONFIRMED'
            THEN seat_id
            ELSE NULL
        END
    ) STORED,

    CONSTRAINT fk_booking_passenger
        FOREIGN KEY (passenger_id)
        REFERENCES passengers(passenger_id),

    CONSTRAINT fk_booking_seat
        FOREIGN KEY (seat_id)
        REFERENCES seats(seat_id),

    CONSTRAINT uq_active_seat
        UNIQUE (active_seat_id),

    INDEX idx_booking_passenger (passenger_id),
    INDEX idx_booking_status (booking_status)
) ENGINE = InnoDB;
