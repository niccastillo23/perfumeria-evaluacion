package com.evaluacion.backend.ms2.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderDocumentListener {

    private static final Logger log = LoggerFactory.getLogger(OrderDocumentListener.class);
    private final RabbitConsumerSupport consumerSupport;

    public OrderDocumentListener(RabbitConsumerSupport consumerSupport) {
        this.consumerSupport = consumerSupport;
    }

    @RabbitListener(queues = "${app.messaging.queues.documents}")
    public void receive(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        consumerSupport.process(message, acknowledger(channel, deliveryTag), OrderMessagingEvent.class, event -> {
            if (event.orderId() == null || event.orderId().isBlank() || event.total() == null || event.total() <= 0) {
                throw new IllegalArgumentException("El documento requiere orderId y total positivo.");
            }
            String receipt = """
                PerfumerIA — COMPROBANTE DE COMPRA
                Orden: %s
                Estado: %s
                Total: $%.2f
                """.formatted(event.orderId(), event.status(), event.total());
            log.info("Documento de compra generado de forma asíncrona:\n{}", receipt);
        }, "documentos de pedidos");
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
