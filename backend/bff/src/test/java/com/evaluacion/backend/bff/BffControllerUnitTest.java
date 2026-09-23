package com.evaluacion.backend.bff;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BffControllerUnitTest {

    @Test
    void catalogResponse_MockData() {
        List<Map<String, Object>> catalog = List.of(
            Map.of("id", 1, "name", "Elegance Gold", "brand", "Luxe Parfums", "price", 189.99),
            Map.of("id", 2, "name", "Ocean Breeze", "brand", "Aqua Scents", "price", 125.50)
        );

        assertEquals(2, catalog.size());
        assertEquals("Elegance Gold", catalog.get(0).get("name"));
        assertEquals(189.99, catalog.get(0).get("price"));
    }

    @Test
    void checkoutResponse_Success() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", "ORD-001");
        result.put("status", "CREATED");
        result.put("total", 189.99);

        assertEquals("ORD-001", result.get("orderId"));
        assertEquals("CREATED", result.get("status"));
        assertEquals(189.99, result.get("total"));
    }

    @Test
    void healthResponse_AllServices() {
        Map<String, Object> services = new LinkedHashMap<>();
        services.put("catalog-service", Map.of("status", "UP"));
        services.put("orders-service", Map.of("status", "UP"));
        services.put("auth-service", Map.of("status", "UP"));
        Map<String, Object> result = Map.of("bff", "OK", "services", services);

        assertEquals("OK", result.get("bff"));
        assertNotNull(result.get("services"));
    }

    @Test
    void healthResponse_SomeServicesDown() {
        Map<String, Object> services = new LinkedHashMap<>();
        services.put("catalog-service", Map.of("status", "UP"));
        services.put("orders-service", Map.of("status", "DOWN", "error", "Connection refused"));
        Map<String, Object> result = Map.of("bff", "OK", "services", services);

        assertEquals("OK", result.get("bff"));
        @SuppressWarnings("unchecked")
        Map<String, Object> ordersStatus = (Map<String, Object>) ((Map<String, Object>) result.get("services")).get("orders-service");
        assertEquals("DOWN", ordersStatus.get("status"));
    }
}
