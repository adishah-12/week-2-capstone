package com.library.repository;

import com.library.config.JpaAuditingConfig;
import com.library.entity.MembershipStatus;
import com.library.entity.Role;
import com.library.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPhoneNumber("+1-555-0100");
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMemberSince(LocalDateTime.now());
        return user;
    }

    @Test
    void findByEmail_returnsUser_whenExists() {
        entityManager.persistAndFlush(buildUser("jane@example.com"));

        Optional<User> found = userRepository.findByEmail("jane@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        Optional<User> found = userRepository.findByEmail("nobody@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_reflectsPersistedState() {
        assertThat(userRepository.existsByEmail("new@example.com")).isFalse();

        entityManager.persistAndFlush(buildUser("new@example.com"));

        assertThat(userRepository.existsByEmail("new@example.com")).isTrue();
    }

    @Test
    void duplicateEmail_violatesUniqueConstraint() {
        entityManager.persistAndFlush(buildUser("dup@example.com"));

        User duplicate = buildUser("dup@example.com");

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(Exception.class);
    }
}