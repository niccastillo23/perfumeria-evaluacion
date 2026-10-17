package com.evaluacion.backend.ms2.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Component
public class RabbitConsumerSupport {

    private static final Logger log = LoggerFactory.getLogger(RabbitConsumerSupport.class);

    private final ObjectMapper objectMapper;
    private final int maxAttempts;
    private final long retryDelayMs;

    public RabbitConsumerSupport(
            ObjectMapper objectMapper,
            @Value("${app.messaging.consumer.max-attempts:3}") int maxAttempts,
            @Value("${app.messaging.consumer.retry-delay-ms:250}") long retryDelayMs) {
        this.objectMapper = objectMapper;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryDelayMs = Math.max(0, retryDelayMs);
    }

    public <T> void process(Message message, Acknowledger acknowledger, Class<T> eventType,
                            Consumer<T> handler, String consumerName) throws IOException {
        T event;
        try {
            event = objectMapper.readValue(message.getBody(), eventType);
        } catch (JsonProcessingException exception) {
            log.error("Mensaje inválido en consumidor {}. Se enviará a DLQ.", consumerName, exception);
            acknowledger.rejectToDeadLetter();
            return;
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                handler.accept(event);
                acknowledger.ack();
                log.debug("ACK manual para mensaje de {} (intento {})", consumerName, attempt);
                return;
            } catch (IllegalArgumentException exception) {
                log.error("Mensaje no recuperable en {}. Se enviará a DLQ: {}", consumerName, exception.getMessage());
                acknowledger.rejectToDeadLetter();
                return;
            } catch (RuntimeException exception) {
                log.warn("Error al procesar mensaje en {} (intento {}/{}): {}",
                    consumerName, attempt, maxAttempts, exception.getMessage());
                if (attempt == maxAttempts) {
                    log.error("Se agotaron los reintentos en {}. Se enviará a DLQ.", consumerName, exception);
                    acknowledger.rejectToDeadLetter();
                    return;
                }
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Reintento interrumpido en {}. Se enviará a DLQ.", consumerName);
                    acknowledger.rejectToDeadLetter();
                    return;
                }
            }
        }
    }

    public interface Acknowledger {
        void ack() throws IOException;
        void rejectToDeadLetter() throws IOException;
    }
}
