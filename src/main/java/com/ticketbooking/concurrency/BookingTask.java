package com.ticketbooking.concurrency;

import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Train;
import com.ticketbooking.service.BookingService;

import java.util.concurrent.CountDownLatch;

public class BookingTask implements Runnable {

    private final BookingService bookingService;
    private final Train train;
    private final Passenger passenger;
    private final int seatNumber;

    private final CountDownLatch readyGate;
    private final CountDownLatch startGate;

    public BookingTask(
            BookingService bookingService,
            Train train,
            Passenger passenger,
            int seatNumber,
            CountDownLatch readyGate,
            CountDownLatch startGate
    ) {
        this.bookingService = bookingService;
        this.train = train;
        this.passenger = passenger;
        this.seatNumber = seatNumber;
        this.readyGate = readyGate;
        this.startGate = startGate;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        System.out.println(
                threadName + " is ready for Passenger "
                        + passenger.getPassengerId()
        );

        readyGate.countDown();

        try {
            // Wait until the main thread releases all workers.
            startGate.await();

            System.out.println(
                    threadName + " processing Passenger "
                            + passenger.getPassengerId()
                            + ", requested seat " + seatNumber
            );

            bookingService.bookTicket(
                    train,
                    passenger,
                    seatNumber
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.out.println(
                    threadName + " was interrupted."
            );
        }
    }
}
