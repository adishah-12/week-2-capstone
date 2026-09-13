package com.library.dto;

import com.library.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class CheckoutResponse {

    private UUID reservationId;
    private ReservationStatus status;
    private LocalDateTime checkedOutAt;
    private LocalDateTime dueDate;
    private String message;

    public CheckoutResponse(UUID reservationId, ReservationStatus status, LocalDateTime checkedOutAt,
                            LocalDateTime dueDate, String message) {
        this.reservationId = reservationId;
        this.status = status;
        this.checkedOutAt = checkedOutAt;
        this.dueDate = dueDate;
        this.message = message;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCheckedOutAt() {
        return checkedOutAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public String getMessage() {
        return message;
    }
}