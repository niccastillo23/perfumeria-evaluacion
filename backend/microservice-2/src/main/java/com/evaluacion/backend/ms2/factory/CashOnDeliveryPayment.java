package com.evaluacion.backend.ms2.factory;

import java.util.Map;
import java.util.UUID;

public class CashOnDeliveryPayment implements PaymentMethod {

    private String transactionId;

    @Override
    public String getType() {
        return "CASH_ON_DELIVERY";
    }

    @Override
    public boolean processPayment(Double amount, Map<String, Object> paymentDetails) {
        this.transactionId = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return true;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }
}
