package com.perfumeria.shipping.messaging;

public record ShippingMessagingEvent(
    String orderId,
    Double total,
    String status,
    String carrier,
    String destination
) {
}
