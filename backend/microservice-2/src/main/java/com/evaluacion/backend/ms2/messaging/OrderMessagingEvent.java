package com.evaluacion.backend.ms2.messaging;

public record OrderMessagingEvent(
    String orderId,
    Double total,
    String status,
    String carrier,
    String destination
) {
}
