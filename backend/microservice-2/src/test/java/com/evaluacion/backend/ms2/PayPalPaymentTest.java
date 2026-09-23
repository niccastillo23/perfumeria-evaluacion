package com.evaluacion.backend.ms2;

import com.evaluacion.backend.ms2.factory.PayPalPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayPalPaymentTest {

    private PayPalPayment payment;

    @BeforeEach
    void setUp() {
        payment = new PayPalPayment();
    }

    @Test
    void getType_ReturnsPayPal() {
        assertEquals("PAYPAL", payment.getType());
    }

    @Test
    void processPayment_ValidEmail_ReturnsTrue() {
        Map<String, Object> details = Map.of("email", "user@example.com");
        boolean result = payment.processPayment(100.0, details);
        assertTrue(result);
        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getTransactionId().startsWith("PP-"));
    }

    @Test
    void processPayment_InvalidEmail_ReturnsFalse() {
        Map<String, Object> details = Map.of("email", "invalid-email");
        boolean result = payment.processPayment(100.0, details);
        assertFalse(result);
    }

    @Test
    void processPayment_NullEmail_ReturnsFalse() {
        Map<String, Object> details = Map.of();
        boolean result = payment.processPayment(100.0, details);
        assertFalse(result);
    }
}
