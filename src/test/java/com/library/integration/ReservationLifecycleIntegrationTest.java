package com.library.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.entity.*;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Boot 4's auto-configured mapper is Jackson 3's JsonMapper, not this classic
    // com.fasterxml ObjectMapper - no bean of this exact type exists to autowire.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String patronToken;
    private String librarianToken;

    @BeforeEach
    void setUp() throws Exception {
        // Registration always assigns PATRON (US-001), so a librarian has to be seeded directly.
        User librarian = new User();
        librarian.setEmail("librarian@example.com");
        librarian.setPassword(passwordEncoder.encode("LibrarianPass1!"));
        librarian.setFirstName("Lib");
        librarian.setLastName("Rarian");
        librarian.setPhoneNumber("+1-555-0199");
        librarian.setRole(Role.LIBRARIAN);
        librarian.setMembershipStatus(MembershipStatus.ACTIVE);
        librarian.setMemberSince(LocalDateTime.now());
        userRepository.save(librarian);

        patronToken = registerAndLogin("patron@example.com");
        librarianToken = login("librarian@example.com", "LibrarianPass1!");
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "SecurePass123!", "firstName": "T",
                         "lastName": "U", "phoneNumber": "+1-555-0100"}""".formatted(email)));
        return login(email, "SecurePass123!");
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}""".formatted(email, password)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private UUID seedBook(int availableCopies) {
        Book book = new Book();
        book.setIsbn(UUID.randomUUID().toString());
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setTotalCopies(5);
        book.setAvailableCopies(availableCopies);
        return bookRepository.save(book).getId();
    }

    @Test
    void fullLifecycle_reserveCheckoutReturn_worksEndToEnd() throws Exception {
        UUID bookId = seedBook(2);

        String reserveResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\": \"" + bookId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andReturn().getResponse().getContentAsString();

        String reservationId = objectMapper.readTree(reserveResponse).get("reservationId").asText();
        assertThat(bookRepository.findById(bookId).get().getAvailableCopies()).isEqualTo(1);

        mockMvc.perform(get("/api/reservations").header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActive").value(1));

        // PATRON cannot checkout - role restricted to LIBRARIAN
        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Good condition\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        mockMvc.perform(post("/api/reservations/" + reservationId + "/return")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\": \"GOOD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lateDays").value(0))
                .andExpect(jsonPath("$.lateFee").value(0.00));

        assertThat(bookRepository.findById(bookId).get().getAvailableCopies()).isEqualTo(2);

        mockMvc.perform(get("/api/reservations/history").header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("RETURNED"))
                .andExpect(jsonPath("$.content[0].wasLate").value(false));
    }

    @Test
    void reservationLimit_sixthReservation_returns400() throws Exception {
        for (int i = 0; i < 5; i++) {
            UUID bookId = seedBook(1);
            mockMvc.perform(post("/api/reservations")
                            .header("Authorization", "Bearer " + patronToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"bookId\": \"" + bookId + "\"}"))
                    .andExpect(status().isCreated());
        }

        UUID sixthBook = seedBook(1);
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\": \"" + sixthBook + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("RESERVATION_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.currentReservations").value(5));
    }

    @Test
    void reserveBookWithNoAvailableCopies_returns400() throws Exception {
        UUID bookId = seedBook(0);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\": \"" + bookId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BOOK_UNAVAILABLE"))
                .andExpect(jsonPath("$.availableCopies").value(0));
    }

    @Test
    void checkoutAlreadyCheckedOutReservation_returns400() throws Exception {
        UUID bookId = seedBook(1);
        String reserveResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\": \"" + bookId + "\"}"))
                .andReturn().getResponse().getContentAsString();
        String reservationId = objectMapper.readTree(reserveResponse).get("reservationId").asText();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // second checkout attempt on the same (now CHECKED_OUT) reservation
        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_STATUS"))
                .andExpect(jsonPath("$.currentStatus").value("CHECKED_OUT"));
    }
}