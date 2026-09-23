package com.perfumeria.shipping.circuitbreaker;

public interface CarrierService {
    String getCarrierName();
    Double calculateShippingCost(Double orderTotal, String destination);
    String generateTrackingNumber(String orderId);
    String requestShipment(String orderId, Double orderTotal, String destination);
}
