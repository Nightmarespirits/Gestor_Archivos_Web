package com.ns.iestpffaaarchives.domain.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class UserTest {

    @Test
    void testUserCreation() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setStatus(true);
        user.setLastLogin(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertTrue(user.getStatus());
        assertNotNull(user.getLastLogin());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testUserEquality() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");
        
        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");
        
        User user3 = new User();
        user3.setId(2L);
        user3.setUsername("otheruser");

        // Assert
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
    }
}
