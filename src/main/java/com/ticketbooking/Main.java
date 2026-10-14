
package com.ticketbooking;

import com.ticketbooking.concurrency.BookingTask;
import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Train;
import com.ticketbooking.service.BookingService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args)
            throws InterruptedException {

        int totalSeats = 1;
        int totalPassengers = 20;
        int poolSize = 5;

        Train train = new Train(
                101,
                "Rajdhani Express",
                totalSeats
        );

        BookingService bookingService = new BookingService();

        ExecutorService executor =
                Executors.newFixedThreadPool(poolSize);

        CountDownLatch readyGate =
                new CountDownLatch(totalPassengers);

        CountDownLatch startGate =
                new CountDownLatch(1);

        System.out.println("===== MULTITHREADED BOOKING SIMULATOR =====");
        System.out.println("Train: " + train.getTrainName());
        System.out.println("Available seats: " + totalSeats);
        System.out.println("Passengers: " + totalPassengers);
        System.out.println("Worker threads: " + poolSize);
        System.out.println("-------------------------------------------");

        try {
            for (int i = 1; i <= totalPassengers; i++) {

                Passenger passenger = new Passenger(
                        i,
                        "Passenger-" + i,
                        "passenger" + i + "@example.com"
                );

                BookingTask task = new BookingTask(
                        bookingService,
                        train,
                        passenger,
                        1,
                        readyGate,
                        startGate
                );

                executor.submit(task);
            }

            // Wait until every task is ready.
            readyGate.await();

            System.out.println("\nAll tasks are ready!");
            System.out.println("Releasing booking requests...\n");

            // Release all waiting tasks.
            startGate.countDown();

        } finally {
            // Shut down after all submitted tasks finish.
            executor.shutdown();

            if (!executor.awaitTermination(
                    30, TimeUnit.SECONDS)) {

                executor.shutdownNow();

                if (!executor.awaitTermination(
                        5, TimeUnit.SECONDS)) {
                    System.err.println(
                            "Some tasks did not terminate."
                    );
                }
            }
        }

        System.out.println("\n===== FINAL RESULTS =====");

        bookingService.displayAllBookings();
        bookingService.displayAvailableSeats(train);

        System.out.println(
                "Bookings recorded: "
                        + bookingService.getBookings().size()
        );

        System.out.println(
                "Available seats: "
                        + train.getAvailableSeats()
        );
    }
}
