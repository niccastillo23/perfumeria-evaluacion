package com.perfumeria.rabbitadmin.api;

import com.perfumeria.rabbitadmin.service.RabbitAdminOperations;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitAdminController {

    private final RabbitAdminOperations rabbitAdminService;

    public RabbitAdminController(RabbitAdminOperations rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createQueue(@Valid @RequestBody QueueRequest request) {
        rabbitAdminService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", request.name(), "status", "created"));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        rabbitAdminService.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(@Valid @RequestBody ExchangeRequest request) {
        rabbitAdminService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", request.name(), "status", "created"));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        rabbitAdminService.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> createBinding(@Valid @RequestBody BindingRequest request) {
        rabbitAdminService.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "created"));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequest request) {
        rabbitAdminService.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }
}
