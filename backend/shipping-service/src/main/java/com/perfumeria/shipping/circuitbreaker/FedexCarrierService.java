package com.perfumeria.shipping.circuitbreaker;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class FedexCarrierService implements CarrierService {

    @Override
    public String getCarrierName() {
        return "FedEx";
    }

    @Override
    public Double calculateShippingCost(Double orderTotal, String destination) {
        return Math.round((orderTotal * 0.08 + 5.0) * 100.0) / 100.0;
    }

    @Override
    public String generateTrackingNumber(String orderId) {
        return "FDX-" + orderId.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public String requestShipment(String orderId, Double orderTotal, String destination) {
        String trackingNumber = generateTrackingNumber(orderId);
        return "FedEx shipment created for order " + orderId + " with tracking: " + trackingNumber;
    }
}
