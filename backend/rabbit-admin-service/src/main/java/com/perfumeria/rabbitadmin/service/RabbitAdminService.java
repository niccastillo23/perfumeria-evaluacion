package com.perfumeria.rabbitadmin.service;

import com.perfumeria.rabbitadmin.api.BindingRequest;
import com.perfumeria.rabbitadmin.api.ExchangeRequest;
import com.perfumeria.rabbitadmin.api.ExchangeType;
import com.perfumeria.rabbitadmin.api.QueueRequest;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitAdminService implements RabbitAdminOperations {

    private final AmqpAdmin rabbitAdmin;

    public RabbitAdminService(AmqpAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    @Override
    public void createQueue(QueueRequest request) {
        Map<String, Object> arguments = new HashMap<>();
        if (request.deadLetterExchange() != null) {
            arguments.put("x-dead-letter-exchange", request.deadLetterExchange());
            arguments.put("x-dead-letter-routing-key", request.deadLetterRoutingKey());
        }
        rabbitAdmin.declareQueue(new Queue(request.name(), request.durable(), request.exclusive(),
            request.autoDelete(), arguments.isEmpty() ? null : arguments));
    }

    @Override
    public void deleteQueue(String name) {
        rabbitAdmin.deleteQueue(name);
    }

    @Override
    public void createExchange(ExchangeRequest request) {
        var exchange = request.type() == ExchangeType.TOPIC
            ? new TopicExchange(request.name(), request.durable(), request.autoDelete())
            : new DirectExchange(request.name(), request.durable(), request.autoDelete());
        rabbitAdmin.declareExchange(exchange);
    }

    @Override
    public void deleteExchange(String name) {
        rabbitAdmin.deleteExchange(name);
    }

    @Override
    public void createBinding(BindingRequest request) {
        rabbitAdmin.declareBinding(binding(request));
    }

    @Override
    public void deleteBinding(BindingRequest request) {
        rabbitAdmin.removeBinding(binding(request));
    }

    private Binding binding(BindingRequest request) {
        return new Binding(request.queueName(), Binding.DestinationType.QUEUE,
            request.exchangeName(), request.routingKey(), null);
    }
}
