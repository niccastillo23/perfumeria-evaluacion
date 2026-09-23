package com.evaluacion.backend.ms2;

import com.evaluacion.backend.ms2.factory.PaymentMethod;
import com.evaluacion.backend.ms2.factory.PaymentMethodFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@SpringBootApplication
@RestController
@RequestMapping("/api/orders")
public class Ms2Application {

    @Autowired
    private OrderRepository orderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(Ms2Application.class, args);
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> orderDetails) {
        String orderId = UUID.randomUUID().toString();
        try {
            String itemsJson = objectMapper.writeValueAsString(orderDetails.get("items"));
            Double total = Double.parseDouble(orderDetails.get("total").toString());

            @SuppressWarnings("unchecked")
            Map<String, Object> paymentDetails = (Map<String, Object>) orderDetails.getOrDefault("payment", Map.of());
            String paymentType = (String) paymentDetails.getOrDefault("type", "cash_on_delivery");

            PaymentMethod paymentMethod = PaymentMethodFactory.getPaymentMethod(paymentType);
            boolean paymentSuccess = paymentMethod.processPayment(total, paymentDetails);

            String status = paymentSuccess ? "PAID" : "PAYMENT_FAILED";
            String transactionId = paymentSuccess ? paymentMethod.getTransactionId() : "N/A";

            OrderEntity newOrder = new OrderEntity(orderId, itemsJson, total, status);
            orderRepository.save(newOrder);

            return Map.of(
                "orderId", orderId,
                "items", orderDetails.get("items"),
                "total", total,
                "status", status,
                "paymentMethod", paymentMethod.getType(),
                "transactionId", transactionId
            );
        } catch (IllegalArgumentException e) {
            return Map.of("error", e.getMessage(), "orderId", orderId);
        } catch (Exception e) {
            return Map.of("error", "Error creating order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrder(@PathVariable String id) {
        Optional<OrderEntity> order = orderRepository.findById(id);
        if (order.isPresent()) {
            OrderEntity o = order.get();
            try {
                Object items = objectMapper.readValue(o.getItemsJson(), Object.class);
                return Map.of(
                    "orderId", o.getOrderId(),
                    "items", items,
                    "total", o.getTotal(),
                    "status", o.getStatus()
                );
            } catch (Exception e) {
                return Map.of("error", "Error parsing order items");
            }
        }
        return Map.of("error", "Order not found");
    }

    @GetMapping
    public java.util.List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/health")
    public String health() {
        return "OK from Orders Microservice";
    }
}
