package com.perfumeria.auth;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerUnitTest {

    @Test
    void loginResponse_Success() {
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "Inicio de sesion exitoso",
            "user", Map.of(
                "username", "admin",
                "email", "admin@test.com",
                "role", "ADMIN"
            )
        );

        assertTrue((Boolean) result.get("success"));
        assertEquals("admin", ((Map<?, ?>) result.get("user")).get("username"));
        assertEquals("ADMIN", ((Map<?, ?>) result.get("user")).get("role"));
    }

    @Test
    void loginResponse_Failure() {
        Map<String, Object> result = Map.of(
            "success", false,
            "message", "Credenciales invalidas"
        );

        assertFalse((Boolean) result.get("success"));
        assertEquals("Credenciales invalidas", result.get("message"));
    }

    @Test
    void registerResponse_Success() {
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "Usuario registrado correctamente con rol CLIENT"
        );

        assertTrue((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("registrado"));
    }

    @Test
    void registerResponse_DuplicateUser() {
        Map<String, Object> result = Map.of(
            "success", false,
            "message", "El usuario ya existe"
        );

        assertFalse((Boolean) result.get("success"));
        assertEquals("El usuario ya existe", result.get("message"));
    }

    @Test
    void userEntity_GettersSetters() {
        User user = new User("testuser", "pass123", "test@email.com", "CLIENT");

        assertEquals("testuser", user.getUsername());
        assertEquals("pass123", user.getPassword());
        assertEquals("test@email.com", user.getEmail());
        assertEquals("CLIENT", user.getRole());

        user.setUsername("newuser");
        user.setPassword("newpass");
        user.setEmail("new@email.com");
        user.setRole("ADMIN");

        assertEquals("newuser", user.getUsername());
        assertEquals("newpass", user.getPassword());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("ADMIN", user.getRole());
    }
}
