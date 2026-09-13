package com.library.exception;

public class InvalidReservationStatusException extends RuntimeException {

    private final String currentStatus;

    public InvalidReservationStatusException(String message, String currentStatus) {
        super(message);
        this.currentStatus = currentStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}