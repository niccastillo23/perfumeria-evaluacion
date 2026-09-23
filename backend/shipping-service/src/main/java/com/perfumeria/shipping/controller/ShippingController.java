package com.perfumeria.shipping.controller;

import com.perfumeria.shipping.model.Shipment;
import com.perfumeria.shipping.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShipment(@RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            String carrier = (String) request.get("carrier");
            Double orderTotal = Double.valueOf(request.get("orderTotal").toString());
            String destination = (String) request.get("destination");

            Shipment shipment = shippingService.createShipment(orderId, carrier, orderTotal, destination);
            return ResponseEntity.ok(shipment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shippingService.getAllShipments());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipment>> getShipmentsByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(shippingService.getShipmentsByOrderId(orderId));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        return shippingService.getShipmentByTrackingNumber(trackingNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Shipment>> getShipmentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(shippingService.getShipmentsByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateShipmentStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> request) {
        try {
            Shipment updated = shippingService.updateShipmentStatus(id, request.get("status"));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/circuit-breaker/state")
    public ResponseEntity<Map<String, String>> getCircuitBreakerState() {
        return ResponseEntity.ok(Map.of("state", shippingService.getCircuitBreakerState()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "shipping-service"));
    }
}
