package com.evaluacion.backend.ms2.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RabbitConsumerSupportTest {

    private final RabbitConsumerSupport support = new RabbitConsumerSupport(new ObjectMapper(), 3, 0);

    @Test
    void acknowledgesMessageAfterSuccessfulProcessing() throws Exception {
        RecordingAcknowledger acknowledger = new RecordingAcknowledger();
        Message message = message("{\"id\":\"order-1\"}", 17L);

        support.process(message, acknowledger, Map.class, event -> {}, "test consumer");

        assertEquals(1, acknowledger.acks);
        assertEquals(0, acknowledger.rejects);
    }

    @Test
    void retriesTransientFailureBeforeAcknowledging() throws Exception {
        RecordingAcknowledger acknowledger = new RecordingAcknowledger();
        Message message = message("{\"id\":\"order-1\"}", 18L);
        AtomicInteger attempts = new AtomicInteger();

        support.process(message, acknowledger, Map.class, event -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("temporary downstream failure");
            }
        }, "test consumer");

        assertEquals(3, attempts.get());
        assertEquals(1, acknowledger.acks);
    }

    @Test
    void rejectsMalformedMessageToDeadLetterExchange() throws Exception {
        RecordingAcknowledger acknowledger = new RecordingAcknowledger();
        Message message = message("{not-json", 19L);

        support.process(message, acknowledger, Map.class, event -> {}, "test consumer");

        assertEquals(1, acknowledger.rejects);
        assertEquals(0, acknowledger.acks);
    }

    private Message message(String body, long deliveryTag) {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(deliveryTag);
        return new Message(body.getBytes(StandardCharsets.UTF_8), properties);
    }

    private static class RecordingAcknowledger implements RabbitConsumerSupport.Acknowledger {
        private int acks;
        private int rejects;

        @Override
        public void ack() {
            acks++;
        }

        @Override
        public void rejectToDeadLetter() {
            rejects++;
        }
    }
}
