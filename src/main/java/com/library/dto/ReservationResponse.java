package com.library.dto;

import com.library.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReservationResponse {

    private UUID reservationId;
    private UUID bookId;
    private UUID userId;
    private String bookTitle;
    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String message;

    public ReservationResponse(UUID reservationId, UUID bookId, UUID userId, String bookTitle,
                               ReservationStatus status, LocalDateTime reservedAt, LocalDateTime expiresAt,
                               String message) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.userId = userId;
        this.bookTitle = bookTitle;
        this.status = status;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getMessage() {
        return message;
    }
}