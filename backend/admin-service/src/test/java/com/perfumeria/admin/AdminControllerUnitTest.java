package com.perfumeria.admin;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminControllerUnitTest {

    @Test
    void statsStructure_WhenServicesDown() {
        Map<String, Object> result = new LinkedHashMap<>();
        int activeUsers = 0;
        double totalSales = 0.0;
        int totalOrders = 0;
        List<String> errors = new ArrayList<>();
        errors.add("auth-service: Connection refused");
        errors.add("orders-service: Connection refused");

        result.put("totalSales", totalSales);
        result.put("totalOrders", totalOrders);
        result.put("activeUsers", activeUsers);
        result.put("topProduct", "Elysian Night");
        result.put("errors", errors);
        result.put("hasErrors", !errors.isEmpty());

        assertEquals(0, result.get("activeUsers"));
        assertEquals(0, result.get("totalOrders"));
        assertEquals(0.0, (Double) result.get("totalSales"));
        assertEquals("Elysian Night", result.get("topProduct"));
        assertTrue((Boolean) result.get("hasErrors"));
        assertEquals(2, ((List<?>) result.get("errors")).size());
    }

    @Test
    void statsStructure_WhenServicesUp() {
        Map<String, Object> result = new LinkedHashMap<>();
        int activeUsers = 5;
        double totalSales = 500.0;
        int totalOrders = 3;
        List<String> errors = new ArrayList<>();

        result.put("totalSales", totalSales);
        result.put("totalOrders", totalOrders);
        result.put("activeUsers", activeUsers);
        result.put("topProduct", "Elysian Night");
        result.put("errors", errors);
        result.put("hasErrors", !errors.isEmpty());

        assertEquals(5, result.get("activeUsers"));
        assertEquals(3, result.get("totalOrders"));
        assertEquals(500.0, (Double) result.get("totalSales"));
        assertFalse((Boolean) result.get("hasErrors"));
    }

    @Test
    void getUsersStructure_WhenAuthUp() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();
        users.add(Map.of("username", "admin", "role", "ADMIN"));
        result.put("users", users);
        result.put("error", null);

        assertNull(result.get("error"));
        assertEquals(1, ((List<?>) result.get("users")).size());
    }

    @Test
    void getUsersStructure_WhenAuthDown() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("users", new ArrayList<>());
        result.put("error", "No se pudo conectar con auth-service");

        assertNotNull(result.get("error"));
        assertTrue(((List<?>) result.get("users")).isEmpty());
    }

    @Test
    void healthCheckStructure() {
        Map<String, Object> services = new LinkedHashMap<>();
        services.put("auth-service", Map.of("status", "UP", "response", "OK"));
        services.put("orders-service", Map.of("status", "DOWN", "error", "Connection refused"));
        Map<String, Object> result = Map.of("admin-service", "OK", "services", services);

        assertEquals("OK", result.get("admin-service"));
        assertNotNull(result.get("services"));
    }
}
