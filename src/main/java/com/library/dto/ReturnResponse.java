package com.library.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReturnResponse {

    private UUID reservationId;
    private LocalDateTime returnedAt;
    private LocalDateTime dueDate;
    private int lateDays;
    private BigDecimal lateFee;
    private String message;

    public ReturnResponse(UUID reservationId, LocalDateTime returnedAt, LocalDateTime dueDate,
                          int lateDays, BigDecimal lateFee, String message) {
        this.reservationId = reservationId;
        this.returnedAt = returnedAt;
        this.dueDate = dueDate;
        this.lateDays = lateDays;
        this.lateFee = lateFee;
        this.message = message;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public int getLateDays() {
        return lateDays;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public String getMessage() {
        return message;
    }
}