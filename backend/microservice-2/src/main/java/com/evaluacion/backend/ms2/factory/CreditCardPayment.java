package com.evaluacion.backend.ms2.factory;

import java.util.Map;
import java.util.UUID;

public class CreditCardPayment implements PaymentMethod {

    private String transactionId;

    @Override
    public String getType() {
        return "CREDIT_CARD";
    }

    @Override
    public boolean processPayment(Double amount, Map<String, Object> paymentDetails) {
        String cardNumber = (String) paymentDetails.get("cardNumber");
        if (cardNumber == null || cardNumber.length() < 16) {
            return false;
        }
        this.transactionId = "CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return true;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }
}
