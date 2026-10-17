package com.perfumeria.rabbitadmin.api;

import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        exception.getBindingResult().getGlobalErrors().stream()
            .map(error -> error.getDefaultMessage())
            .forEach(errors::add);
        return ResponseEntity.badRequest().body(Map.of("error", "Parámetros inválidos", "details", errors));
    }

    @ExceptionHandler(AmqpException.class)
    ResponseEntity<Map<String, String>> handleRabbitFailure(AmqpException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "RabbitMQ no pudo completar la operación administrativa."));
    }
}
