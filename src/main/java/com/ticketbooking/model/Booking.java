
package com.ticketbooking.model;

import java.time.LocalDateTime;

public class Booking {

    private final int bookingId;
    private final Passenger passenger;
    private final Train train;
    private final Seat seat;
    private final LocalDateTime bookingTime;

    public Booking(
            int bookingId,
            Passenger passenger,
            Train train,
            Seat seat
    ) {
        this.bookingId = bookingId;
        this.passenger = passenger;
        this.train = train;
        this.seat = seat;
        this.bookingTime = LocalDateTime.now();
    }

    public int getBookingId() {
        return bookingId;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public Train getTrain() {
        return train;
    }

    public Seat getSeat() {
        return seat;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    @Override
    public String toString() {
        return "Booking ID: " + bookingId
                + "\n" + passenger
                + "\nTrain: " + train.getTrainName()
                + "\nSeat Number: " + seat.getSeatNumber()
                + "\nBooking Time: " + bookingTime;
    }
}
