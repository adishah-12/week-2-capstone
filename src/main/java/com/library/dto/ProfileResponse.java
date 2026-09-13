package com.library.dto;

import com.library.entity.MembershipStatus;
import com.library.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProfileResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;
    private MembershipStatus membershipStatus;
    private LocalDateTime memberSince;
    private long activeReservations;
    private long borrowingHistory;

    public ProfileResponse(UUID userId, String email, String firstName, String lastName, String phoneNumber,
                           Role role, MembershipStatus membershipStatus, LocalDateTime memberSince,
                           long activeReservations, long borrowingHistory) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.membershipStatus = membershipStatus;
        this.memberSince = memberSince;
        this.activeReservations = activeReservations;
        this.borrowingHistory = borrowingHistory;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public MembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    public LocalDateTime getMemberSince() {
        return memberSince;
    }

    public long getActiveReservations() {
        return activeReservations;
    }

    public long getBorrowingHistory() {
        return borrowingHistory;
    }
}