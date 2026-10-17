package com.perfumeria.shipping.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShippingRabbitConfiguration {

    @Bean
    DirectExchange shippingDirectExchange(@Value("${app.messaging.exchanges.shipping}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    DirectExchange shippingDeadLetterExchange(@Value("${app.messaging.exchanges.dead-letter}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    Queue shippingRequestsQueue(
            @Value("${app.messaging.queues.shipping-requests}") String name,
            @Value("${app.messaging.exchanges.dead-letter}") String deadLetterExchange,
            @Value("${app.messaging.routing-keys.shipping-dead}") String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
            .deadLetterExchange(deadLetterExchange)
            .deadLetterRoutingKey(deadLetterRoutingKey)
            .build();
    }

    @Bean
    Queue shippingRequestsDlq(@Value("${app.messaging.queues.shipping-requests-dlq}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    Binding shippingRequestsBinding(
            @Qualifier("shippingRequestsQueue") Queue queue,
            @Qualifier("shippingDirectExchange") DirectExchange exchange,
            @Value("${app.messaging.routing-keys.shipping-requested}") String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    Binding shippingRequestsDlqBinding(
            @Qualifier("shippingRequestsDlq") Queue queue,
            @Qualifier("shippingDeadLetterExchange") DirectExchange exchange,
            @Value("${app.messaging.routing-keys.shipping-dead}") String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(10);
        return factory;
    }
}
