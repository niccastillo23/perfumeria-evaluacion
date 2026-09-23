package com.evaluacion.backend.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1")
public class BffApplication {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // URLs de microservicios (en un entorno real vendrían de configuración)
    private final String CATALOG_SERVICE_URL = "http://localhost:8081/api/catalog";
    private final String ORDER_SERVICE_URL = "http://localhost:8082/api/orders";

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }

    @GetMapping("/shop/catalog")
    public List<Map<String, Object>> getCatalog() {
        // En un entorno real llamaríamos al MS1
        // return restTemplate.getForObject(CATALOG_SERVICE_URL, List.class);
        
        // Mock para demostración
        return List.of(
            Map.of("id", 1, "name", "Elegance Gold", "brand", "Luxe Parfums", "price", 85.00),
            Map.of("id", 2, "name", "Midnight Rain", "brand", "Aqua Essence", "price", 120.00)
        );
    }

    @PostMapping("/shop/checkout")
    public Map<String, Object> checkout(@RequestBody Map<String, Object> orderRequest) {
        // Orquestación: Podría validar stock en MS1 y luego crear orden en MS2
        // return restTemplate.postForObject(ORDER_SERVICE_URL, orderRequest, Map.class);
        
        return Map.of("orderId", "BFF-" + System.currentTimeMillis(), "status", "PROCESSED_BY_BFF");
    }

    @GetMapping("/health")
    public String health() {
        return "OK from Perfume E-commerce BFF";
    }
}
