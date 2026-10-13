
package com.ticketbooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Train {

    private final int trainId;
    private final String trainName;
    private final List<Seat> seats;

    public Train(int trainId, String trainName, int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException(
                    "Total seats must be greater than zero."
            );
        }

        this.trainId = trainId;
        this.trainName = trainName;
        this.seats = new ArrayList<>();

        for (int i = 1; i <= totalSeats; i++) {
            seats.add(new Seat(i));
        }
    }

    public int getTrainId() {
        return trainId;
    }

    public String getTrainName() {
        return trainName;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public int getAvailableSeats() {
        int available = 0;

        for (Seat seat : seats) {
            if (!seat.isBooked()) {
                available++;
            }
        }

        return available;
    }

    @Override
    public String toString() {
        return "Train ID: " + trainId
                + ", Name: " + trainName
                + ", Available Seats: " + getAvailableSeats();
    }
}
