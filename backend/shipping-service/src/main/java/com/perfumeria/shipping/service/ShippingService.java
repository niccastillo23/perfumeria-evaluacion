package com.perfumeria.shipping.service;

import com.perfumeria.shipping.circuitbreaker.CarrierFactory;
import com.perfumeria.shipping.circuitbreaker.CarrierService;
import com.perfumeria.shipping.model.Shipment;
import com.perfumeria.shipping.repository.ShipmentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);
    private static final String CB_NAME = "carrierApi";

    private final ShipmentRepository shipmentRepository;
    private final CarrierFactory carrierFactory;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ShippingService(ShipmentRepository shipmentRepository,
                           CarrierFactory carrierFactory,
                           CircuitBreakerRegistry circuitBreakerRegistry) {
        this.shipmentRepository = shipmentRepository;
        this.carrierFactory = carrierFactory;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "createShipmentFallback")
    public Shipment createShipment(String orderId, String carrierName,
                                   Double orderTotal, String destination) {
        log.info("Creando envio para orden {} con transportista {}", orderId, carrierName);

        CarrierService carrier = carrierFactory.getCarrier(carrierName);
        Double cost = carrier.calculateShippingCost(orderTotal, destination);
        String result = carrier.requestShipment(orderId, orderTotal, destination);
        String trackingNumber = carrier.generateTrackingNumber(orderId);

        log.info("Resultado del transportista: {}", result);

        Shipment shipment = new Shipment(
            orderId,
            carrier.getCarrierName(),
            trackingNumber,
            "PENDING",
            destination,
            cost
        );

        return shipmentRepository.save(shipment);
    }

    public Shipment createShipmentFallback(String orderId, String carrierName,
                                           Double orderTotal, String destination,
                                           Throwable t) {
        log.warn("Circuit Breaker activado para orden {}: {}. Usando fallback.", orderId, t.getMessage());

        String trackingNumber = "FB-" + orderId.toUpperCase() + "-FALLBACK";
        Shipment fallbackShipment = new Shipment(
            orderId,
            carrierName.toUpperCase(),
            trackingNumber,
            "PENDING_FALLBACK",
            destination,
            0.0
        );

        return shipmentRepository.save(fallbackShipment);
    }

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public List<Shipment> getShipmentsByOrderId(String orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    public List<Shipment> getShipmentsByStatus(String status) {
        return shipmentRepository.findByStatus(status);
    }

    public Shipment updateShipmentStatus(Long id, String newStatus) {
        Optional<Shipment> optional = shipmentRepository.findById(id);
        if (optional.isPresent()) {
            Shipment shipment = optional.get();
            shipment.setStatus(newStatus);
            return shipmentRepository.save(shipment);
        }
        throw new RuntimeException("Envio no encontrado con id: " + id);
    }

    public String getCircuitBreakerState() {
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb =
            circuitBreakerRegistry.circuitBreaker(CB_NAME);
        return cb.getState().toString();
    }
}
