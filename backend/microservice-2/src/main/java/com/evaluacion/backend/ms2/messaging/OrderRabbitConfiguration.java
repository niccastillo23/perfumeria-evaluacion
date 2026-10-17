package com.evaluacion.backend.ms2.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderRabbitConfiguration {

    @Bean
    TopicExchange ordersTopicExchange(@Value("${app.messaging.exchanges.orders}") String name) {
        return new TopicExchange(name, true, false);
    }

    @Bean
    DirectExchange shippingDirectExchange(@Value("${app.messaging.exchanges.shipping}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    DirectExchange deadLetterDirectExchange(@Value("${app.messaging.exchanges.dead-letter}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    Queue orderNotificationsQueue(
            @Value("${app.messaging.queues.notifications}") String name,
            @Value("${app.messaging.exchanges.dead-letter}") String deadLetterExchange,
            @Value("${app.messaging.routing-keys.notifications-dead}") String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
            .deadLetterExchange(deadLetterExchange)
            .deadLetterRoutingKey(deadLetterRoutingKey)
            .build();
    }

    @Bean
    Queue orderNotificationsDlq(@Value("${app.messaging.queues.notifications-dlq}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    Queue orderDocumentsQueue(
            @Value("${app.messaging.queues.documents}") String name,
            @Value("${app.messaging.exchanges.dead-letter}") String deadLetterExchange,
            @Value("${app.messaging.routing-keys.documents-dead}") String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
            .deadLetterExchange(deadLetterExchange)
            .deadLetterRoutingKey(deadLetterRoutingKey)
            .build();
    }

    @Bean
    Queue orderDocumentsDlq(@Value("${app.messaging.queues.documents-dlq}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    Binding orderNotificationsBinding(
            @Qualifier("orderNotificationsQueue") Queue orderNotificationsQueue,
            @Qualifier("ordersTopicExchange") TopicExchange ordersTopicExchange,
            @Value("${app.messaging.routing-keys.notifications-pattern}") String routingKey) {
        return BindingBuilder.bind(orderNotificationsQueue).to(ordersTopicExchange).with(routingKey);
    }

    @Bean
    Binding orderNotificationsDlqBinding(
            @Qualifier("orderNotificationsDlq") Queue orderNotificationsDlq,
            @Qualifier("deadLetterDirectExchange") DirectExchange deadLetterDirectExchange,
            @Value("${app.messaging.routing-keys.notifications-dead}") String routingKey) {
        return BindingBuilder.bind(orderNotificationsDlq).to(deadLetterDirectExchange).with(routingKey);
    }

    @Bean
    Binding orderDocumentsBinding(
            @Qualifier("orderDocumentsQueue") Queue orderDocumentsQueue,
            @Qualifier("ordersTopicExchange") TopicExchange ordersTopicExchange,
            @Value("${app.messaging.routing-keys.order-paid}") String routingKey) {
        return BindingBuilder.bind(orderDocumentsQueue).to(ordersTopicExchange).with(routingKey);
    }

    @Bean
    Binding orderDocumentsDlqBinding(
            @Qualifier("orderDocumentsDlq") Queue orderDocumentsDlq,
            @Qualifier("deadLetterDirectExchange") DirectExchange deadLetterDirectExchange,
            @Value("${app.messaging.routing-keys.documents-dead}") String routingKey) {
        return BindingBuilder.bind(orderDocumentsDlq).to(deadLetterDirectExchange).with(routingKey);
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
