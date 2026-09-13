package com.library.dto;

import com.library.entity.Role;

import java.util.UUID;

public class UserSummary {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public UserSummary(UUID userId, String email, String firstName, String lastName, Role role) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
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
}