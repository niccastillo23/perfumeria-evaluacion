package com.evaluacion.backend.ms2;

import com.evaluacion.backend.ms2.factory.CreditCardPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardPaymentTest {

    private CreditCardPayment payment;

    @BeforeEach
    void setUp() {
        payment = new CreditCardPayment();
    }

    @Test
    void getType_ReturnsCreditCard() {
        assertEquals("CREDIT_CARD", payment.getType());
    }

    @Test
    void processPayment_ValidCard_ReturnsTrue() {
        Map<String, Object> details = Map.of("cardNumber", "4111111111111111");
        boolean result = payment.processPayment(150.0, details);
        assertTrue(result);
        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getTransactionId().startsWith("CC-"));
    }

    @Test
    void processPayment_NullCardNumber_ReturnsFalse() {
        Map<String, Object> details = Map.of();
        boolean result = payment.processPayment(150.0, details);
        assertFalse(result);
        assertNull(payment.getTransactionId());
    }

    @Test
    void processPayment_ShortCardNumber_ReturnsFalse() {
        Map<String, Object> details = Map.of("cardNumber", "1234");
        boolean result = payment.processPayment(150.0, details);
        assertFalse(result);
    }

    @Test
    void getTransactionId_AfterPayment_IsNotNull() {
        Map<String, Object> details = Map.of("cardNumber", "4111111111111111");
        payment.processPayment(200.0, details);
        String txId = payment.getTransactionId();
        assertNotNull(txId);
        assertTrue(txId.matches("CC-[A-Z0-9]{8}"));
    }
}
