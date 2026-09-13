package com.library.dto;

import com.library.entity.MembershipStatus;
import com.library.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private MembershipStatus membershipStatus;
    private LocalDateTime createdAt;
    private String message;

    public RegisterResponse(UUID userId, String email, String firstName, String lastName,
                            Role role, MembershipStatus membershipStatus, LocalDateTime createdAt, String message) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.membershipStatus = membershipStatus;
        this.createdAt = createdAt;
        this.message = message;
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

    public Role getRole() {
        return role;
    }

    public MembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
}