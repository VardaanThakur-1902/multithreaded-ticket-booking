
package com.ticketbooking;

import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Train;
import com.ticketbooking.service.BookingService;

public class Main {

    public static void main(String[] args) {

        Train train = new Train(
                101,
                "Rajdhani Express",
                5
        );

        BookingService bookingService = new BookingService();

        Passenger passenger1 = new Passenger(
                1,
                "Rahul Sharma",
                "rahul@example.com"
        );

        Passenger passenger2 = new Passenger(
                2,
                "Priya Verma",
                "priya@example.com"
        );

        // Display initial seat availability
        bookingService.displayAvailableSeats(train);

        // Book seat 1 for passenger 1
        bookingService.bookTicket(train, passenger1, 1);

        // Attempt to book the same seat for passenger 2
        bookingService.bookTicket(train, passenger2, 1);

        // Book another seat for passenger 2
        bookingService.bookTicket(train, passenger2, 2);

        // Display all active bookings
        bookingService.displayAllBookings();

        // Display remaining seats
        bookingService.displayAvailableSeats(train);

        // Cancel booking 1
        bookingService.cancelBooking(1);

        // Display final availability
        bookingService.displayAvailableSeats(train);

        System.out.println("\nSimulation completed.");
    }
}
