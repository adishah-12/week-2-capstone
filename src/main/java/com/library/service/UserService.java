package com.library.service;

import com.library.dto.ProfileResponse;
import com.library.entity.ReservationStatus;
import com.library.entity.User;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long activeReservations = reservationRepository.countByUserIdAndStatusIn(
                userId, List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );
        long borrowingHistory = reservationRepository.countByUserIdAndStatus(
                userId, ReservationStatus.RETURNED
        );

        return new ProfileResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
                user.getRole(), user.getMembershipStatus(), user.getMemberSince(),
                activeReservations, borrowingHistory
        );
    }
}