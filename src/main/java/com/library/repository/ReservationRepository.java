package com.library.repository;

import com.library.entity.Reservation;
import com.library.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // Active = RESERVED or CHECKED_OUT.
    List<Reservation> findByUserIdAndStatusIn(UUID userId, List<ReservationStatus> statuses);

    long countByUserIdAndStatusIn(UUID userId, List<ReservationStatus> statuses);

    // Profile's "borrowingHistory" status
    long countByUserIdAndStatus(UUID userId, ReservationStatus status);

    // Full history, all statuses, paginated.
    Page<Reservation> findByUserId(UUID userId, Pageable pageable);
}