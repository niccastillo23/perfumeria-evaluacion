package com.evaluacion.backend.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1")
public class BffApplication {

    private final RestTemplate restTemplate;

    private final String catalogServiceUrl;
    private final String orderServiceUrl;
    private final String profileServiceUrl;
    private final String adminServiceUrl;

    public BffApplication(
            RestTemplate restTemplate,
            @Value("${app.services.catalog-url}") String catalogServiceUrl,
            @Value("${app.services.orders-url}") String orderServiceUrl,
            @Value("${app.services.profile-url}") String profileServiceUrl,
            @Value("${app.services.admin-url}") String adminServiceUrl) {
        this.restTemplate = restTemplate;
        this.catalogServiceUrl = catalogServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.profileServiceUrl = profileServiceUrl;
        this.adminServiceUrl = adminServiceUrl;
    }

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }

    @GetMapping("/shop/catalog")
    @PreAuthorize("hasAuthority('SCOPE_Catalog.Read')")
    public ResponseEntity<Object> getCatalog() {
        return restTemplate.getForEntity(catalogServiceUrl, Object.class);
    }

    @PostMapping("/shop/checkout")
    @PreAuthorize("hasAuthority('SCOPE_Orders.Create')")
    public ResponseEntity<Object> checkout(@RequestBody Map<String, Object> orderRequest) {
        return restTemplate.postForEntity(orderServiceUrl, orderRequest, Object.class);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<Object> getProfile(@PathVariable String username) {
        return restTemplate.getForEntity(profileServiceUrl + "/" + username, Object.class);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('SCOPE_Admin.Read')")
    public ResponseEntity<Object> getAdminStats() {
        return restTemplate.getForEntity(adminServiceUrl + "/stats", Object.class);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('SCOPE_Admin.Read')")
    public ResponseEntity<Object> getAdminUsers() {
        return restTemplate.getForEntity(adminServiceUrl + "/users", Object.class);
    }

    @GetMapping("/health")
    public String health() {
        return "OK from Perfume E-commerce BFF";
    }
}
