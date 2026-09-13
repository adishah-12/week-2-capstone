package com.library.dto;

import java.util.List;

public class ActiveReservationsResponse {

    private List<ActiveReservationItem> reservations;
    private int totalActive;

    public ActiveReservationsResponse(List<ActiveReservationItem> reservations) {
        this.reservations = reservations;
        this.totalActive = reservations.size();
    }

    public List<ActiveReservationItem> getReservations() {
        return reservations;
    }

    public int getTotalActive() {
        return totalActive;
    }
}