package com.evaluacion.backend.bff;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/rabbitmq")
@PreAuthorize("hasAuthority('SCOPE_Admin.Read')")
public class RabbitAdminProxyController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String rabbitAdminServiceUrl;

    public RabbitAdminProxyController(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.services.rabbit-admin-url}") String rabbitAdminServiceUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.rabbitAdminServiceUrl = rabbitAdminServiceUrl;
    }

    @PostMapping("/queues")
    public ResponseEntity<Object> createQueue(@RequestBody Object request) {
        return forward("/queues", HttpMethod.POST, request);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Object> deleteQueue(@PathVariable String name) {
        return forward("/queues/" + UriUtils.encodePathSegment(name, StandardCharsets.UTF_8), HttpMethod.DELETE, null);
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Object> createExchange(@RequestBody Object request) {
        return forward("/exchanges", HttpMethod.POST, request);
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Object> deleteExchange(@PathVariable String name) {
        return forward("/exchanges/" + UriUtils.encodePathSegment(name, StandardCharsets.UTF_8), HttpMethod.DELETE, null);
    }

    @PostMapping("/bindings")
    public ResponseEntity<Object> createBinding(@RequestBody Object request) {
        return forward("/bindings", HttpMethod.POST, request);
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Object> deleteBinding(@RequestBody Object request) {
        return forward("/bindings", HttpMethod.DELETE, request);
    }

    private ResponseEntity<Object> forward(String path, HttpMethod method, Object body) {
        try {
            ResponseEntity<Object> downstream = restTemplate.exchange(
                rabbitAdminServiceUrl + path, method, new HttpEntity<>(body), Object.class);
            // The BFF creates a new HTTP response; forwarding hop-by-hop headers can corrupt chunked responses.
            return ResponseEntity.status(downstream.getStatusCode()).body(downstream.getBody());
        } catch (RestClientResponseException exception) {
            Object errorBody;
            try {
                errorBody = objectMapper.readValue(exception.getResponseBodyAsByteArray(), Object.class);
            } catch (IOException parseException) {
                errorBody = Map.of("error", "RabbitMQ admin rejected the request.");
            }
            return ResponseEntity.status(exception.getStatusCode()).body(errorBody);
        }
    }
}
