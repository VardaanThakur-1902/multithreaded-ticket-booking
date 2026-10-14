package com.ticketbooking.service;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Train;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


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
    
    @Test
    void shouldAllowOnlyOneBookingWhenTwentyPassengersCompete()
            throws Exception {

        int passengerCount = 20;

        CountDownLatch startGate = new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(10);

        List<Future<Optional<Booking>>> futures =
                new ArrayList<>();

        try {
            for (int i = 1; i <= passengerCount; i++) {
                final int passengerId = i;

                Future<Optional<Booking>> future =
                        executor.submit(() -> {
                            Passenger p = new Passenger(
                                    passengerId,
                                    "Passenger-" + passengerId,
                                    "passenger" + passengerId
                                            + "@example.com"
                            );

                            startGate.await();

                            return bookingService.bookTicket(
                                    train, p, 1
                            );
                        });

                futures.add(future);
            }

            // Release all submitted tasks together.
            startGate.countDown();

            int successfulBookings = 0;

            for (Future<Optional<Booking>> future : futures) {
                if (future.get(10, TimeUnit.SECONDS).isPresent()) {
                    successfulBookings++;
                }
            }

            assertEquals(1, successfulBookings);
            assertEquals(2, train.getAvailableSeats());
            assertEquals(1, bookingService.getBookings().size());

        } finally {
            startGate.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(
                    5, TimeUnit.SECONDS
            ));
        }
    }

}

