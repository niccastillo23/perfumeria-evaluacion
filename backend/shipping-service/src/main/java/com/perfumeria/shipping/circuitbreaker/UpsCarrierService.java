package com.perfumeria.shipping.circuitbreaker;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UpsCarrierService implements CarrierService {

    @Override
    public String getCarrierName() {
        return "UPS";
    }

    @Override
    public Double calculateShippingCost(Double orderTotal, String destination) {
        return Math.round((orderTotal * 0.07 + 6.0) * 100.0) / 100.0;
    }

    @Override
    public String generateTrackingNumber(String orderId) {
        return "UPS-" + orderId.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public String requestShipment(String orderId, Double orderTotal, String destination) {
        String trackingNumber = generateTrackingNumber(orderId);
        return "UPS shipment created for order " + orderId + " with tracking: " + trackingNumber;
    }
}
