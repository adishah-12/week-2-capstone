package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.dto.RegisterResponse;
import com.library.dto.UserSummary;
import com.library.entity.MembershipStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.AuthenticationFailedException;
import com.library.exception.EmailAlreadyExistsException;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMemberSince(LocalDateTime.now());

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName(),
                saved.getRole(), saved.getMembershipStatus(), saved.getCreatedAt(),
                "Registration successful"
        );
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                // Same message either way - don't reveal whether the email exists.
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        UserSummary summary = new UserSummary(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole()
        );

        return new LoginResponse(token, jwtService.getExpirationSeconds(), summary);
    }
}