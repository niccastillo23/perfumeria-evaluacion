package com.evaluacion.backend.ms2.messaging;

import org.springframework.amqp.AmqpException;

public interface OrderMessageSender {
    void send(String exchange, String routingKey, byte[] payload) throws AmqpException;
}
