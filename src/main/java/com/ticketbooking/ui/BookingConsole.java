
package com.ticketbooking.ui;

import com.ticketbooking.dao.BookingDao;
import com.ticketbooking.dao.BookingDao.BookingDetails;
import com.ticketbooking.dao.BookingDao.TrainAvailability;
import com.ticketbooking.service.TransactionalBookingService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BookingConsole {

    private final Scanner scanner = new Scanner(System.in);
    private final BookingDao bookingDao = new BookingDao();
    private final TransactionalBookingService bookingService =
            new TransactionalBookingService();

    public static void main(String[] args) {
        new BookingConsole().start();
    }

    public void start() {
        boolean running = true;

        System.out.println("==================================");
        System.out.println(" MULTITHREADED TICKET BOOKING");
        System.out.println("==================================");

        while (running) {
            printMenu();

            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> searchTrains();
                    case 2 -> bookTicket();
                    case 3 -> viewBookingHistory();
                    case 4 -> checkBookingStatus();
                    case 5 -> cancelTicket();
                    case 0 -> {
                        running = false;
                        System.out.println("Application closed.");
                    }
                    default -> System.out.println(
                            "Invalid option. Please choose again."
                    );
                }
            } catch (SQLException e) {
                System.err.println(
                        "Database operation failed: " + e.getMessage()
                );
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("Request rejected: " + e.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }

    private void printMenu() {
        System.out.println("""
            
            -------- MAIN MENU --------
            1. Search trains
            2. Book a ticket
            3. View passenger booking history
            4. Check booking status
            5. Cancel a ticket
            0. Exit
            ---------------------------
            """);
    }

    private void searchTrains() throws SQLException {
        System.out.println(
                "Leave a station blank to search without that filter."
        );

        System.out.print("Source station: ");
        String source = scanner.nextLine().trim();

        System.out.print("Destination station: ");
        String destination = scanner.nextLine().trim();

        List<TrainAvailability> trains =
                bookingDao.searchTrains(source, destination);

        if (trains.isEmpty()) {
            System.out.println("No matching trains found.");
            return;
        }

        System.out.println("\nMatching trains:");

        for (TrainAvailability train : trains) {
            System.out.printf(
                    "Train ID: %d | %s | %s -> %s | "
                            + "Seats: %d/%d available%n",
                    train.trainId(),
                    train.trainName(),
                    train.source(),
                    train.destination(),
                    train.availableSeats(),
                    train.totalSeats()
            );
        }
    }

    private void bookTicket() throws SQLException {
        int trainId = readInt("Enter train ID: ");
        int passengerId = readInt("Enter passenger ID: ");
        int seatNumber = readInt("Enter seat number: ");

        long bookingId = bookingService.bookTicket(
                trainId,
                passengerId,
                seatNumber
        );

        System.out.println(
                "Your booking reference is: " + bookingId
        );
    }

    private void viewBookingHistory() throws SQLException {
        long passengerId = readLong("Enter passenger ID: ");

        List<BookingDetails> history =
                bookingDao.getBookingHistory(passengerId);

        if (history.isEmpty()) {
            System.out.println("No booking history found.");
            return;
        }

        System.out.println("\n----- BOOKING HISTORY -----");

        for (BookingDetails booking : history) {
            printBooking(booking);
        }
    }

    private void checkBookingStatus() throws SQLException {
        long bookingId = readLong("Enter booking ID: ");

        Optional<String> status =
                bookingDao.getBookingStatus(bookingId);

        if (status.isPresent()) {
            System.out.println(
                    "Booking " + bookingId
                            + " status: " + status.get()
            );
        } else {
            System.out.println("Booking ID not found.");
        }
    }

    private void cancelTicket() throws SQLException {
        long bookingId = readLong("Enter booking ID to cancel: ");

        boolean cancelled = bookingService.cancelTicket(bookingId);

        if (!cancelled) {
            System.out.println(
                    "Cancellation rejected. Check the booking ID "
                            + "and current booking status."
            );
        }
    }

    private void printBooking(BookingDetails booking) {
        System.out.printf(
                "Booking ID: %d | Passenger: %s | Train: %s | "
                        + "Seat: %d | Status: %s | Time: %s%n",
                booking.bookingId(),
                booking.passengerName(),
                booking.trainName(),
                booking.seatNumber(),
                booking.status(),
                booking.bookingTime()
        );
    }

    private int readInt(String prompt) {
        return Math.toIntExact(readLong(prompt));
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid whole number."
                );
            }
        }
    }
}
