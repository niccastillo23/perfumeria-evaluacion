package com.perfumeria.admin;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final RestTemplate restTemplate;

    public AdminController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AdminController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        int activeUsers = 0;
        double totalSales = 0.0;
        int totalOrders = 0;
        List<String> errors = new ArrayList<>();

        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                "http://localhost:8083/api/auth/users", List.class);
            List<?> users = response.getBody();
            activeUsers = users != null ? users.size() : 0;
        } catch (Exception e) {
            errors.add("auth-service: " + e.getMessage());
            System.err.println("Error fetching users: " + e.getMessage());
        }

        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                "http://localhost:8082/api/orders", List.class);
            List<?> orders = response.getBody();
            totalOrders = orders != null ? orders.size() : 0;
            if (orders != null) {
                for (Object orderObj : orders) {
                    if (orderObj instanceof Map) {
                        Map<?, ?> order = (Map<?, ?>) orderObj;
                        Object total = order.get("total");
                        if (total != null) {
                            totalSales += Double.parseDouble(total.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            errors.add("orders-service: " + e.getMessage());
            System.err.println("Error fetching orders: " + e.getMessage());
        }

        result.put("totalSales", totalSales);
        result.put("totalOrders", totalOrders);
        result.put("activeUsers", activeUsers);
        result.put("topProduct", "Elysian Night");
        result.put("errors", errors);
        result.put("hasErrors", !errors.isEmpty());
        return result;
    }

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                "http://localhost:8083/api/auth/users", List.class);
            List<?> users = response.getBody();
            result.put("users", users != null ? users : new ArrayList<>());
            result.put("error", null);
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            result.put("users", new ArrayList<>());
            result.put("error", "No se pudo conectar con auth-service: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> services = new LinkedHashMap<>();
        checkService(services, "auth-service", "http://localhost:8083/api/auth/health");
        checkService(services, "orders-service", "http://localhost:8082/api/orders/health");
        checkService(services, "catalog-service", "http://localhost:8081/health");
        checkService(services, "profile-service", "http://localhost:8084/api/profile/health");
        return Map.of("admin-service", "OK", "services", services);
    }

    private void checkService(Map<String, Object> services, String name, String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            services.put(name, Map.of("status", "UP", "response", response.getBody()));
        } catch (Exception e) {
            services.put(name, Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
}
