package com.ticketbooking.service;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Train;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingService bookingService;
    private Train train;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService();
        train = new Train(101, "Rajdhani Express", 3);
        passenger = new Passenger(
                1, "Rahul Sharma", "rahul@example.com"
        );
    }

    @Test
    void shouldBookAvailableSeat() {
        Optional<Booking> result =
                bookingService.bookTicket(train, passenger, 1);

        assertTrue(result.isPresent());
        assertEquals(2, train.getAvailableSeats());
        assertEquals(1, bookingService.getBookings().size());
    }

    @Test
    void shouldRejectDuplicateSeatBooking() {
        bookingService.bookTicket(train, passenger, 1);

        Passenger anotherPassenger = new Passenger(
                2, "Priya Verma", "priya@example.com"
        );

        Optional<Booking> result =
                bookingService.bookTicket(train, anotherPassenger, 1);

        assertTrue(result.isEmpty());
        assertEquals(2, train.getAvailableSeats());
        assertEquals(1, bookingService.getBookings().size());
    }

    @Test
    void shouldRejectInvalidSeatNumber() {
        Optional<Booking> result =
                bookingService.bookTicket(train, passenger, 99);

        assertTrue(result.isEmpty());
        assertEquals(3, train.getAvailableSeats());
        assertEquals(0, bookingService.getBookings().size());
    }

    @Test
    void shouldReleaseSeatAfterCancellation() {
        Optional<Booking> result =
                bookingService.bookTicket(train, passenger, 1);

        assertTrue(result.isPresent());

        int bookingId = result.orElseThrow().getBookingId();

        assertTrue(bookingService.cancelBooking(bookingId));
        assertEquals(3, train.getAvailableSeats());
        assertEquals(0, bookingService.getBookings().size());
    }

    @Test
    void shouldRejectRepeatedCancellation() {
        Optional<Booking> result =
                bookingService.bookTicket(train, passenger, 1);

        assertTrue(result.isPresent());

        int bookingId = result.orElseThrow().getBookingId();

        assertTrue(bookingService.cancelBooking(bookingId));
        assertFalse(bookingService.cancelBooking(bookingId));

        assertEquals(3, train.getAvailableSeats());
    }

    @Test
    void shouldRejectBookingWhenTrainIsSoldOut() {
        bookingService.bookTicket(train, passenger, 1);
        bookingService.bookTicket(train, passenger, 2);
        bookingService.bookTicket(train, passenger, 3);

        Optional<Booking> result =
                bookingService.bookTicket(train, passenger, 1);

        assertTrue(result.isEmpty());
        assertEquals(0, train.getAvailableSeats());
        assertEquals(3, bookingService.getBookings().size());
    }
}

