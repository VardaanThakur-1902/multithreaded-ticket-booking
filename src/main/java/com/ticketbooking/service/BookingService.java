
package com.ticketbooking.service;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Seat;
import com.ticketbooking.model.Train;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingService {

    private final List<Booking> bookings = new ArrayList<>();
    private int nextBookingId = 1;

    // Display available seats.
    public synchronized void displayAvailableSeats(Train train) {
        System.out.println(
                "\nAvailable seats for " + train.getTrainName() + ":"
        );

        boolean found = false;

        for (Seat seat : train.getSeats()) {
            if (!seat.isBooked()) {
                System.out.print(seat.getSeatNumber() + " ");
                found = true;
            }
        }

        if (!found) {
            System.out.print("No seats available.");
        }

        System.out.println();
    }

    // Book a seat atomically through this service.
    public synchronized Optional<Booking> bookTicket(
            Train train,
            Passenger passenger,
            int seatNumber
    ) {
        Seat selectedSeat = null;

        for (Seat seat : train.getSeats()) {
            if (seat.getSeatNumber() == seatNumber) {
                selectedSeat = seat;
                break;
            }
        }

        if (selectedSeat == null) {
            System.out.println(
                    "Booking failed: Invalid seat number."
            );
            return Optional.empty();
        }

        if (selectedSeat.isBooked()) {
            System.out.println(
                    "Booking failed: Seat " + seatNumber
                            + " is already booked."
            );
            return Optional.empty();
        }

        // Check and update happen under the same service lock.
        selectedSeat.setBooked(true);

        Booking booking = new Booking(
                nextBookingId++,
                passenger,
                train,
                selectedSeat
        );

        bookings.add(booking);

        System.out.println(
                "Ticket booked successfully by "
                        + passenger.getName()
                        + " | Booking ID: "
                        + booking.getBookingId()
                        + " | Thread: "
                        + Thread.currentThread().getName()
        );

        return Optional.of(booking);
    }

    // Cancellation uses the same lock as booking.
    public synchronized boolean cancelBooking(int bookingId) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);

            if (booking.getBookingId() == bookingId) {
                booking.getSeat().setBooked(false);
                bookings.remove(i);

                System.out.println(
                        "Booking " + bookingId
                                + " cancelled successfully."
                );

                return true;
            }
        }

        System.out.println(
                "Cancellation failed: Booking not found."
        );
        return false;
    }

    public synchronized void displayAllBookings() {
        System.out.println("\n===== ALL ACTIVE BOOKINGS =====");

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Booking booking : bookings) {
            System.out.println(booking);
            System.out.println("-------------------------");
        }
    }

    // Return a snapshot instead of exposing the mutable list.
    public synchronized List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }
}
