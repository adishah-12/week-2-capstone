package com.library.security;

import com.library.entity.Role;

import java.util.UUID;

// The Authentication principal for every authenticated request. Built directly from the
// JWT's claims in JwtAuthenticationFilter - no DB lookup needed just to authorize a request.
public record AuthenticatedUser(UUID id, String email, Role role) {
}