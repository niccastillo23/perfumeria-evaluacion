package com.perfumeria.shipping.messaging;

import com.perfumeria.shipping.service.ShippingService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ShipmentRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentRequestedListener.class);

    private final RabbitConsumerSupport consumerSupport;
    private final ShippingService shippingService;

    public ShipmentRequestedListener(RabbitConsumerSupport consumerSupport, ShippingService shippingService) {
        this.consumerSupport = consumerSupport;
        this.shippingService = shippingService;
    }

    @RabbitListener(queues = "${app.messaging.queues.shipping-requests}")
    public void receive(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        consumerSupport.process(message, acknowledger(channel, deliveryTag), ShippingMessagingEvent.class, event -> {
            if (event.orderId() == null || event.orderId().isBlank()
                    || event.total() == null || event.total() <= 0
                    || event.destination() == null || event.destination().isBlank()) {
                throw new IllegalArgumentException("La solicitud de envío contiene datos incompletos.");
            }

            var shipment = shippingService.createShipment(
                event.orderId(), event.carrier(), event.total(), event.destination());
            log.info("Solicitud de envío procesada: orden={}, tracking={}",
                event.orderId(), shipment.getTrackingNumber());
        }, "solicitudes de envío");
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
