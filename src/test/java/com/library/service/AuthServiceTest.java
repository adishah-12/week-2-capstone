package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.RegisterRequest;
import com.library.entity.MembershipStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.AuthenticationFailedException;
import com.library.exception.EmailAlreadyExistsException;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPhoneNumber("+1-555-0100");
    }

    @Test
    void register_success_defaultsToPatronAndActive() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        var response = authService.register(registerRequest);

        assertThat(response.getRole()).isEqualTo(Role.PATRON);
        assertThat(response.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(response.getMessage()).isEqualTo("Registration successful");

        verify(userRepository).save(argThat(u -> u.getPassword().equals("hashed")));
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExists() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsTokenAndUserSummary() {
        User user = existingUser();
        LoginRequest request = new LoginRequest();
        request.setEmail("existing@example.com");
        request.setPassword("correct-password");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        var response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUser().getEmail()).isEqualTo("existing@example.com");
    }

    @Test
    void login_unknownEmail_throwsGenericMessage() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_wrongPassword_throwsSameGenericMessageAsUnknownEmail() {
        User user = existingUser();
        LoginRequest request = new LoginRequest();
        request.setEmail("existing@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        // Deliberately checking the message is identical to the unknown-email case -
        // this is what makes login non-enumerable per US-002.
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");
    }

    private User existingUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("existing@example.com");
        user.setPassword("hashed-password");
        user.setFirstName("Existing");
        user.setLastName("User");
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMemberSince(LocalDateTime.now());
        return user;
    }
}