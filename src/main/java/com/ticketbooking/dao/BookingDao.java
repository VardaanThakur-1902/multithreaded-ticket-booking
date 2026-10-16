
package com.ticketbooking.dao;

import com.ticketbooking.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDao {

    // Data returned by train search.
    public record TrainAvailability(
            int trainId,
            String trainName,
            String source,
            String destination,
            int totalSeats,
            int availableSeats
    ) {}

    // Data returned by booking history.
    public record BookingDetails(
            long bookingId,
            String passengerName,
            String trainName,
            int seatNumber,
            String bookingTime,
            String status
    ) {}

    // Search trains and calculate currently available seats.
    public List<TrainAvailability> searchTrains(
            String source,
            String destination
    ) throws SQLException {

        String sql = """
            SELECT
                t.train_id,
                t.train_name,
                t.source_station,
                t.destination_station,
                t.total_seats,
                SUM(
                    CASE WHEN s.seat_status = 'AVAILABLE'
                    THEN 1 ELSE 0 END
                ) AS available_seats
            FROM trains t
            LEFT JOIN seats s ON t.train_id = s.train_id
            WHERE (? IS NULL OR t.source_station = ?)
              AND (? IS NULL OR t.destination_station = ?)
            GROUP BY
                t.train_id,
                t.train_name,
                t.source_station,
                t.destination_station,
                t.total_seats
            ORDER BY t.train_id
            """;

        List<TrainAvailability> trains = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            setOptionalString(statement, 1, source);
            setOptionalString(statement, 2, source);
            setOptionalString(statement, 3, destination);
            setOptionalString(statement, 4, destination);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    trains.add(new TrainAvailability(
                            rs.getInt("train_id"),
                            rs.getString("train_name"),
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getInt("total_seats"),
                            rs.getInt("available_seats")
                    ));
                }
            }
        }

        return trains;
    }

    // View all bookings for a passenger, including cancelled bookings.
    public List<BookingDetails> getBookingHistory(
            long passengerId
    ) throws SQLException {

        String sql = """
            SELECT
                b.booking_id,
                p.passenger_name,
                t.train_name,
                s.seat_number,
                b.booking_time,
                b.booking_status
            FROM bookings b
            JOIN passengers p
                ON b.passenger_id = p.passenger_id
            JOIN seats s
                ON b.seat_id = s.seat_id
            JOIN trains t
                ON s.train_id = t.train_id
            WHERE b.passenger_id = ?
            ORDER BY b.booking_time DESC, b.booking_id DESC
            """;

        List<BookingDetails> history = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, passengerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    history.add(new BookingDetails(
                            rs.getLong("booking_id"),
                            rs.getString("passenger_name"),
                            rs.getString("train_name"),
                            rs.getInt("seat_number"),
                            rs.getTimestamp("booking_time").toString(),
                            rs.getString("booking_status")
                    ));
                }
            }
        }

        return history;
    }

    // Get the current status of a booking.
    public Optional<String> getBookingStatus(
            long bookingId
    ) throws SQLException {

        String sql = """
            SELECT booking_status
            FROM bookings
            WHERE booking_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, bookingId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                            rs.getString("booking_status")
                    );
                }
            }
        }

        return Optional.empty();
    }

    private void setOptionalString(
            PreparedStatement statement,
            int index,
            String value
    ) throws SQLException {

        String normalized = value == null || value.isBlank()
                ? null
                : value.trim();

        statement.setString(index, normalized);
    }
}
