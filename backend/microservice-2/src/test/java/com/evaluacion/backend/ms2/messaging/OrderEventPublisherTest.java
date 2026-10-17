package com.evaluacion.backend.ms2.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderEventPublisherTest {

    private final RecordingMessageSender messageSender = new RecordingMessageSender();
    private final OrderEventPublisher publisher = new OrderEventPublisher(
        messageSender,
        new ObjectMapper(),
        "orders.topic",
        "shipping.direct",
        "orders.created",
        "orders.paid",
        "shipping.requested"
    );

    @Test
    void paidOrderPublishesNotificationDocumentAndShipmentTasks() {
        publisher.publish(new OrderMessagingEvent("order-1", 75.0, "PAID", "dhl", "Santiago"));

        assertEquals(List.of(
            new PublishedMessage("orders.topic", "orders.created"),
            new PublishedMessage("orders.topic", "orders.paid"),
            new PublishedMessage("shipping.direct", "shipping.requested")
        ), messageSender.messages);
    }

    @Test
    void failedPaymentDoesNotRequestDocumentOrShipment() {
        publisher.publish(new OrderMessagingEvent("order-2", 75.0, "PAYMENT_FAILED", "dhl", "Santiago"));

        assertEquals(List.of(new PublishedMessage("orders.topic", "orders.created")), messageSender.messages);
    }

    private record PublishedMessage(String exchange, String routingKey) {}

    private static class RecordingMessageSender implements OrderMessageSender {
        private final List<PublishedMessage> messages = new ArrayList<>();

        @Override
        public void send(String exchange, String routingKey, byte[] payload) {
            messages.add(new PublishedMessage(exchange, routingKey));
        }
    }
}
