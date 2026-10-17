package com.evaluacion.backend.ms2.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);
    private final RabbitConsumerSupport consumerSupport;

    public OrderNotificationListener(RabbitConsumerSupport consumerSupport) {
        this.consumerSupport = consumerSupport;
    }

    @RabbitListener(queues = "${app.messaging.queues.notifications}")
    public void receive(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        consumerSupport.process(message, acknowledger(channel, deliveryTag), OrderMessagingEvent.class, event -> {
            if (event.orderId() == null || event.orderId().isBlank()) {
                throw new IllegalArgumentException("La notificación requiere un orderId.");
            }
            log.info("Notificación {} procesada: orden={}, estado={}",
                message.getMessageProperties().getReceivedRoutingKey(), event.orderId(), event.status());
        }, "notificaciones de pedidos");
    }

    private RabbitConsumerSupport.Acknowledger acknowledger(Channel channel, long deliveryTag) {
        return new RabbitConsumerSupport.Acknowledger() {
            @Override
            public void ack() throws IOException {
                channel.basicAck(deliveryTag, false);
            }

            @Override
            public void rejectToDeadLetter() throws IOException {
                channel.basicNack(deliveryTag, false, false);
            }
        };
    }
}
