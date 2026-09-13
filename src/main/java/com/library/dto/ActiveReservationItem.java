package com.library.dto;

import com.library.entity.Reservation;
import com.library.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ActiveReservationItem {

    private UUID reservationId;
    private UUID bookId;
    private String bookTitle;
    private String bookAuthor;
    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private Long daysUntilExpiry;
    private LocalDateTime checkedOutAt;
    private LocalDateTime dueDate;
    private Long daysUntilDue;

    public ActiveReservationItem(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.bookId = reservation.getBook().getId();
        this.bookTitle = reservation.getBook().getTitle();
        this.bookAuthor = reservation.getBook().getAuthor();
        this.status = reservation.getStatus();

        LocalDateTime now = LocalDateTime.now();
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            this.reservedAt = reservation.getReservedAt();
            this.expiresAt = reservation.getExpiresAt();
            this.daysUntilExpiry = ChronoUnit.DAYS.between(now.toLocalDate(), reservation.getExpiresAt().toLocalDate());
        } else if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            this.checkedOutAt = reservation.getCheckedOutAt();
            this.dueDate = reservation.getDueDate();
            this.daysUntilDue = ChronoUnit.DAYS.between(now.toLocalDate(), reservation.getDueDate().toLocalDate());
        }
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
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

    public Long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public LocalDateTime getCheckedOutAt() {
        return checkedOutAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Long getDaysUntilDue() {
        return daysUntilDue;
    }
}