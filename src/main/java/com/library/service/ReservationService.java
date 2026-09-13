package com.library.service;

import com.library.dto.*;
import com.library.entity.Book;
import com.library.entity.Reservation;
import com.library.entity.ReservationStatus;
import com.library.exception.BookUnavailableException;
import com.library.exception.InvalidReservationStatusException;
import com.library.exception.ReservationLimitExceededException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ReservationService {

    private static final int MAX_ACTIVE_RESERVATIONS = 5;
    private static final int RESERVATION_EXPIRY_DAYS = 7;
    private static final int CHECKOUT_PERIOD_DAYS = 14;
    private static final BigDecimal LATE_FEE_PER_DAY = BigDecimal.valueOf(1.00);
    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse createReservation(UUID userId, UUID bookId) {
        long activeCount = reservationRepository.countByUserIdAndStatusIn(
                userId, List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );
        if (activeCount >= MAX_ACTIVE_RESERVATIONS) {
            throw new ReservationLimitExceededException(
                    "You have reached the maximum of 5 active reservations", activeCount);
        }

        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No copies available for reservation",
                    book.getAvailableCopies() == null ? 0 : book.getAvailableCopies());
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.RESERVED);
        LocalDateTime now = LocalDateTime.now();
        reservation.setReservedAt(now);
        reservation.setExpiresAt(now.plusDays(RESERVATION_EXPIRY_DAYS));

        Reservation saved = reservationRepository.save(reservation);

        return new ReservationResponse(
                saved.getId(), book.getId(), userId, book.getTitle(), saved.getStatus(),
                saved.getReservedAt(), saved.getExpiresAt(),
                "Book reserved successfully. Please pick up within 7 days."
        );
    }

    public ActiveReservationsResponse getActiveReservations(UUID userId) {
        List<Reservation> active = reservationRepository.findByUserIdAndStatusIn(
                userId, List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );
        List<ActiveReservationItem> items = active.stream().map(ActiveReservationItem::new).toList();
        return new ActiveReservationsResponse(items);
    }

    @Transactional
    public CheckoutResponse checkout(UUID reservationId, String notes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InvalidReservationStatusException(
                    "Can only checkout reservations with RESERVED status", reservation.getStatus().name());
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setCheckedOutAt(now);
        reservation.setDueDate(now.plusDays(CHECKOUT_PERIOD_DAYS));
        reservation.setNotes(notes);

        Reservation saved = reservationRepository.save(reservation);

        String message = "Book checked out successfully. Due date: " + saved.getDueDate().format(DUE_DATE_FORMAT);
        return new CheckoutResponse(saved.getId(), saved.getStatus(), saved.getCheckedOutAt(), saved.getDueDate(), message);
    }

    @Transactional
    public ReturnResponse processReturn(UUID reservationId, ReturnRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            throw new InvalidReservationStatusException(
                    "Can only return books with CHECKED_OUT status", reservation.getStatus().name());
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnedAt(now);
        reservation.setCondition(request.getCondition());
        reservation.setNotes(request.getNotes());

        int lateDays = 0;
        BigDecimal lateFee = BigDecimal.ZERO.setScale(2);
        if (now.isAfter(reservation.getDueDate())) {
            // Calendar-date diff, not exact 24h periods - matches the contract's worked example
            // (due 10/13 3pm, returned 10/15 10am -> 2 late days despite being <48h apart).
            lateDays = (int) ChronoUnit.DAYS.between(reservation.getDueDate().toLocalDate(), now.toLocalDate());
            if (lateDays < 1) {
                lateDays = 1;
            }
            lateFee = LATE_FEE_PER_DAY.multiply(BigDecimal.valueOf(lateDays)).setScale(2);
        }
        reservation.setLateDays(lateDays);
        reservation.setLateFee(lateFee);

        Book book = bookRepository.findByIdForUpdate(reservation.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Reservation saved = reservationRepository.save(reservation);

        String message = lateDays > 0
                ? String.format("Book returned. Late fee of $%.2f applied to account.", lateFee)
                : "Book returned successfully";

        return new ReturnResponse(saved.getId(), saved.getReturnedAt(), saved.getDueDate(),
                lateDays, lateFee, message);
    }

    public PagedResponse<HistoryItem> getHistory(UUID userId, int page, int size) {
        var pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reservedAt"));
        var result = reservationRepository.findByUserId(userId, pageRequest);
        List<HistoryItem> items = result.getContent().stream().map(HistoryItem::new).toList();
        return new PagedResponse<>(items, result);
    }
}