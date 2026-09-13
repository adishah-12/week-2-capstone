package com.library.repository;

import com.library.config.JpaAuditingConfig;
import com.library.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ReservationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    private User patron;
    private Book book;

    @BeforeEach
    void seed() {
        patron = new User();
        patron.setEmail("patron@example.com");
        patron.setPassword("hashed");
        patron.setFirstName("Pat");
        patron.setLastName("Ron");
        patron.setPhoneNumber("+1-555-0100");
        patron.setRole(Role.PATRON);
        patron.setMembershipStatus(MembershipStatus.ACTIVE);
        patron.setMemberSince(LocalDateTime.now());
        entityManager.persistAndFlush(patron);

        book = new Book();
        book.setIsbn("999");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
        entityManager.persistAndFlush(book);

        // One RESERVED, one CHECKED_OUT, one RETURNED, one CANCELLED - covers all statuses for history.
        entityManager.persistAndFlush(reservation(ReservationStatus.RESERVED, LocalDateTime.now().plusDays(7), null, null));
        entityManager.persistAndFlush(reservation(ReservationStatus.CHECKED_OUT, null, LocalDateTime.now(), LocalDateTime.now().plusDays(14)));
        entityManager.persistAndFlush(reservation(ReservationStatus.RETURNED, null, null, null));
        entityManager.persistAndFlush(reservation(ReservationStatus.CANCELLED, null, null, null));
    }

    private Reservation reservation(ReservationStatus status, LocalDateTime expiresAt,
                                    LocalDateTime checkedOutAt, LocalDateTime dueDate) {
        Reservation r = new Reservation();
        r.setBook(book);
        r.setUser(patron);
        r.setStatus(status);
        r.setReservedAt(LocalDateTime.now());
        r.setExpiresAt(expiresAt);
        r.setCheckedOutAt(checkedOutAt);
        r.setDueDate(dueDate);
        return r;
    }

    @Test
    void findByUserIdAndStatusIn_returnsOnlyActiveReservations() {
        List<Reservation> active = reservationRepository.findByUserIdAndStatusIn(
                patron.getId(), List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT));

        assertThat(active).hasSize(2);
        assertThat(active).extracting(Reservation::getStatus)
                .containsExactlyInAnyOrder(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT);
    }

    @Test
    void countByUserIdAndStatusIn_countsOnlyActive() {
        long count = reservationRepository.countByUserIdAndStatusIn(
                patron.getId(), List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT));

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByUserIdAndStatus_countsCompletedReservations() {
        long returned = reservationRepository.countByUserIdAndStatus(patron.getId(), ReservationStatus.RETURNED);

        assertThat(returned).isEqualTo(1);
    }

    @Test
    void findByUserId_paginatedHistory_includesAllStatuses() {
        var page = reservationRepository.findByUserId(patron.getId(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "reservedAt")));

        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    void findByUserId_unknownUser_returnsEmptyPage() {
        var page = reservationRepository.findByUserId(UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void reservation_resolvesBookAndUserRelationships() {
        Reservation any = reservationRepository.findByUserIdAndStatusIn(
                patron.getId(), List.of(ReservationStatus.RESERVED)).get(0);

        assertThat(any.getBook().getTitle()).isEqualTo("Test Book");
        assertThat(any.getUser().getEmail()).isEqualTo("patron@example.com");
    }
}