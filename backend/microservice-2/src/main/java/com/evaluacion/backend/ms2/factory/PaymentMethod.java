package com.evaluacion.backend.ms2.factory;

import java.util.Map;

public interface PaymentMethod {
    String getType();
    boolean processPayment(Double amount, Map<String, Object> paymentDetails);
    String getTransactionId();
}
