package com.library.controllers;

import com.library.dto.*;
import com.library.security.AuthenticatedUser;
import com.library.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @Valid @RequestBody ReserveRequest request) {
        ReservationResponse response = reservationService.createReservation(user.id(), request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ActiveReservationsResponse getActive(@AuthenticationPrincipal AuthenticatedUser user) {
        return reservationService.getActiveReservations(user.id());
    }

    // Access restricted to LIBRARIAN role in SecurityConfig - no role check needed here.
    @PostMapping("/{reservationId}/checkout")
    public CheckoutResponse checkout(@PathVariable UUID reservationId,
                                     @RequestBody(required = false) CheckoutRequest request) {
        String notes = request != null ? request.getNotes() : null;
        return reservationService.checkout(reservationId, notes);
    }

    // Access restricted to LIBRARIAN role in SecurityConfig - no role check needed here.
    @PostMapping("/{reservationId}/return")
    public ReturnResponse processReturn(@PathVariable UUID reservationId,
                                        @Valid @RequestBody ReturnRequest request) {
        return reservationService.processReturn(reservationId, request);
    }

    @GetMapping("/history")
    public PagedResponse<HistoryItem> history(@AuthenticationPrincipal AuthenticatedUser user,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return reservationService.getHistory(user.id(), page, size);
    }
}