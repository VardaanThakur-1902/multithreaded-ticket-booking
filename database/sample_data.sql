
USE ticket_booking_db;

-- Add a sample train.
INSERT IGNORE INTO trains (
    train_id,
    train_name,
    source_station,
    destination_station,
    total_seats
)
VALUES (
    101,
    'Rajdhani Express',
    'New Delhi',
    'Mumbai Central',
    5
);

-- Create five seats for the sample train.
INSERT IGNORE INTO seats (train_id, seat_number)
SELECT 101, seat_number
FROM (
    SELECT 1 AS seat_number
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
) AS seat_numbers;

-- Add sample passengers.
INSERT IGNORE INTO passengers (
    passenger_name,
    email
)
VALUES
    ('Rahul Sharma', 'rahul@example.com'),
    ('Priya Verma', 'priya@example.com'),
    ('Amit Kumar', 'amit@example.com');
