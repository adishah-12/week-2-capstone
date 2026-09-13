package com.library.dto;

import com.library.entity.Reservation;
import com.library.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoryItem {

    private UUID reservationId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime reservedAt;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
    private LocalDateTime dueDate;
    private ReservationStatus status;
    private boolean wasLate;

    public HistoryItem(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.bookTitle = reservation.getBook().getTitle();
        this.bookAuthor = reservation.getBook().getAuthor();
        this.reservedAt = reservation.getReservedAt();
        this.checkedOutAt = reservation.getCheckedOutAt();
        this.returnedAt = reservation.getReturnedAt();
        this.dueDate = reservation.getDueDate();
        this.status = reservation.getStatus();
        this.wasLate = reservation.getReturnedAt() != null
                && reservation.getDueDate() != null
                && reservation.getReturnedAt().isAfter(reservation.getDueDate());
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public LocalDateTime getCheckedOutAt() {
        return checkedOutAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isWasLate() {
        return wasLate;
    }
}