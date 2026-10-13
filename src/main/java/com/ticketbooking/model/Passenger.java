
package com.ticketbooking.model;

public class Passenger {

    private final int passengerId;
    private final String name;
    private final String email;

    public Passenger(int passengerId, String name, String email) {
        this.passengerId = passengerId;
        this.name = name;
        this.email = email;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Passenger ID: " + passengerId
                + ", Name: " + name
                + ", Email: " + email;
    }
}
