package com.evaluacion.backend.ms2.factory;

import java.util.Map;
import java.util.UUID;

public class PayPalPayment implements PaymentMethod {

    private String transactionId;

    @Override
    public String getType() {
        return "PAYPAL";
    }

    @Override
    public boolean processPayment(Double amount, Map<String, Object> paymentDetails) {
        String email = (String) paymentDetails.get("email");
        if (email == null || !email.contains("@")) {
            return false;
        }
        this.transactionId = "PP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return true;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }
}
