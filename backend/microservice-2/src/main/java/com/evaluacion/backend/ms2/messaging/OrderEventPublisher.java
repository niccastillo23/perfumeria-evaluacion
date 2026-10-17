package com.evaluacion.backend.ms2.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final OrderMessageSender messageSender;
    private final ObjectMapper objectMapper;
    private final String ordersExchange;
    private final String shippingExchange;
    private final String orderCreatedRoutingKey;
    private final String orderPaidRoutingKey;
    private final String shippingRequestedRoutingKey;

    public OrderEventPublisher(
            OrderMessageSender messageSender,
            ObjectMapper objectMapper,
            @Value("${app.messaging.exchanges.orders}") String ordersExchange,
            @Value("${app.messaging.exchanges.shipping}") String shippingExchange,
            @Value("${app.messaging.routing-keys.order-created}") String orderCreatedRoutingKey,
            @Value("${app.messaging.routing-keys.order-paid}") String orderPaidRoutingKey,
            @Value("${app.messaging.routing-keys.shipping-requested}") String shippingRequestedRoutingKey) {
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.ordersExchange = ordersExchange;
        this.shippingExchange = shippingExchange;
        this.orderCreatedRoutingKey = orderCreatedRoutingKey;
        this.orderPaidRoutingKey = orderPaidRoutingKey;
        this.shippingRequestedRoutingKey = shippingRequestedRoutingKey;
    }

    public void publish(OrderMessagingEvent event) {
        publish(ordersExchange, orderCreatedRoutingKey, event, "aviso de pedido");

        if ("PAID".equalsIgnoreCase(event.status())) {
            publish(ordersExchange, orderPaidRoutingKey, event, "generación de documento");
            publish(shippingExchange, shippingRequestedRoutingKey, event, "solicitud de envío");
        }
    }

    private void publish(String exchange, String routingKey, OrderMessagingEvent event, String task) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(event);
            messageSender.send(exchange, routingKey, payload);
            log.info("Evento de {} publicado para la orden {}", task, event.orderId());
        } catch (JsonProcessingException | AmqpException exception) {
            // La orden ya está guardada; una caída del broker no debe cambiar la respuesta del checkout.
            log.error("No se pudo publicar la tarea de {} para la orden {}", task, event.orderId(), exception);
        }
    }
}
