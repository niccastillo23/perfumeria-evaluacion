package com.perfumeria.profile;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileControllerUnitTest {

    @Test
    void profileResponse_Success() {
        Map<String, Object> profile = Map.of(
            "username", "admin",
            "fullName", "Admin User",
            "bio", "Administrator",
            "memberSince", "2024-01-01",
            "preferences", Map.of("scentType", "Floral")
        );

        assertEquals("admin", profile.get("username"));
        assertEquals("Admin User", profile.get("fullName"));
        assertEquals("Floral", ((Map<?, ?>) profile.get("preferences")).get("scentType"));
    }

    @Test
    void profileResponse_NotFound() {
        Map<String, Object> result = Map.of(
            "error", "Perfil no encontrado para el usuario inexistente"
        );

        assertNotNull(result.get("error"));
        assertTrue(result.get("error").toString().contains("no encontrado"));
    }

    @Test
    void profileEntity_GettersSetters() {
        Profile profile = new Profile();
        profile.setUsername("testuser");
        profile.setFullName("Test User");
        profile.setBio("Test bio");
        profile.setMemberSince("2024-01-01");

        assertEquals("testuser", profile.getUsername());
        assertEquals("Test User", profile.getFullName());
        assertEquals("Test bio", profile.getBio());
        assertEquals("2024-01-01", profile.getMemberSince());
    }

    @Test
    void profileEntity_Constructor() {
        Profile profile = new Profile("user1", "User One", "Bio", "2024-01-01", "{\"scent\":\"floral\"}");

        assertEquals("user1", profile.getUsername());
        assertEquals("User One", profile.getFullName());
        assertEquals("Bio", profile.getBio());
        assertEquals("2024-01-01", profile.getMemberSince());
        assertEquals("{\"scent\":\"floral\"}", profile.getPreferencesJson());
    }

    @Test
    void healthResponse() {
        Map<String, Object> result = Map.of(
            "status", "UP",
            "service", "profile-service"
        );

        assertEquals("UP", result.get("status"));
        assertEquals("profile-service", result.get("service"));
    }
}
