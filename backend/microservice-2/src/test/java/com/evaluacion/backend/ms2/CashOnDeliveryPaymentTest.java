package com.evaluacion.backend.ms2;

import com.evaluacion.backend.ms2.factory.CashOnDeliveryPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CashOnDeliveryPaymentTest {

    private CashOnDeliveryPayment payment;

    @BeforeEach
    void setUp() {
        payment = new CashOnDeliveryPayment();
    }

    @Test
    void getType_ReturnsCashOnDelivery() {
        assertEquals("CASH_ON_DELIVERY", payment.getType());
    }

    @Test
    void processPayment_AlwaysReturnsTrue() {
        boolean result = payment.processPayment(100.0, Map.of());
        assertTrue(result);
        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getTransactionId().startsWith("COD-"));
    }

    @Test
    void getTransactionId_AfterPayment_IsNotNull() {
        payment.processPayment(50.0, Map.of());
        String txId = payment.getTransactionId();
        assertNotNull(txId);
        assertTrue(txId.matches("COD-[A-Z0-9]{8}"));
    }
}
