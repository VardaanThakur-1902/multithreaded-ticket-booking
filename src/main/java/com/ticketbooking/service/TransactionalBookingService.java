
package com.ticketbooking.service;

import com.ticketbooking.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionalBookingService {

    /**
     * Books a seat using a database transaction.
     *
     * @return the generated booking ID
     */
    public long bookTicket(int trainId, int passengerId, int seatNumber)
            throws SQLException {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                long seatId;
                String seatStatus;

                // Lock the seat row until the transaction finishes.
                String selectSeat = """
                    SELECT seat_id, seat_status
                    FROM seats
                    WHERE train_id = ? AND seat_number = ?
                    FOR UPDATE
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSeat)) {

                    statement.setInt(1, trainId);
                    statement.setInt(2, seatNumber);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                    "Invalid train or seat number."
                            );
                        }

                        seatId = result.getLong("seat_id");
                        seatStatus = result.getString("seat_status");
                    }
                }

                if (!"AVAILABLE".equals(seatStatus)) {
                    throw new IllegalStateException(
                            "Seat is already booked."
                    );
                }

                // Reserve the seat.
                String updateSeat = """
                    UPDATE seats
                    SET seat_status = 'BOOKED'
                    WHERE seat_id = ?
                      AND seat_status = 'AVAILABLE'
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSeat)) {

                    statement.setLong(1, seatId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Seat is no longer available."
                        );
                    }
                }

                // Create the confirmed booking.
                String insertBooking = """
                    INSERT INTO bookings
                        (passenger_id, seat_id, booking_status)
                    VALUES (?, ?, 'CONFIRMED')
                    """;

                long bookingId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertBooking,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, passengerId);
                    statement.setLong(2, seatId);

                    statement.executeUpdate();

                    try (ResultSet keys =
                                 statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Failed to generate booking ID."
                            );
                        }

                        bookingId = keys.getLong(1);
                    }
                }

                // Both changes succeed together.
                connection.commit();

                System.out.println(
                        "Booking confirmed. ID: " + bookingId
                );

                return bookingId;

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }

    /**
     * Cancels an active booking and releases its seat.
     */
    public boolean cancelTicket(long bookingId) throws SQLException {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                long seatId;
                String bookingStatus;

                String selectBooking = """
                    SELECT seat_id, booking_status
                    FROM bookings
                    WHERE booking_id = ?
                    FOR UPDATE
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectBooking)) {

                    statement.setLong(1, bookingId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        seatId = result.getLong("seat_id");
                        bookingStatus =
                                result.getString("booking_status");
                    }
                }

                if (!"CONFIRMED".equals(bookingStatus)) {
                    connection.rollback();
                    return false;
                }

                String cancelBooking = """
                    UPDATE bookings
                    SET booking_status = 'CANCELLED'
                    WHERE booking_id = ?
                      AND booking_status = 'CONFIRMED'
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(cancelBooking)) {

                    statement.setLong(1, bookingId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Booking could not be cancelled."
                        );
                    }
                }

                String releaseSeat = """
                    UPDATE seats
                    SET seat_status = 'AVAILABLE'
                    WHERE seat_id = ?
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(releaseSeat)) {

                    statement.setLong(1, seatId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not release the seat."
                        );
                    }
                }

                connection.commit();

                System.out.println(
                        "Booking " + bookingId
                                + " cancelled successfully."
                );

                return true;

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }
}
