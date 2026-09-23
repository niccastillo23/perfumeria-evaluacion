package com.evaluacion.backend.ms2;

import com.evaluacion.backend.ms2.factory.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodFactoryTest {

    @Test
    void getPaymentMethod_CreditCard_ReturnsCreditCardPayment() {
        PaymentMethod method = PaymentMethodFactory.getPaymentMethod("credit_card");
        assertNotNull(method);
        assertInstanceOf(CreditCardPayment.class, method);
        assertEquals("CREDIT_CARD", method.getType());
    }

    @Test
    void getPaymentMethod_PayPal_ReturnsPayPalPayment() {
        PaymentMethod method = PaymentMethodFactory.getPaymentMethod("paypal");
        assertNotNull(method);
        assertInstanceOf(PayPalPayment.class, method);
        assertEquals("PAYPAL", method.getType());
    }

    @Test
    void getPaymentMethod_CashOnDelivery_ReturnsCashOnDeliveryPayment() {
        PaymentMethod method = PaymentMethodFactory.getPaymentMethod("cash_on_delivery");
        assertNotNull(method);
        assertInstanceOf(CashOnDeliveryPayment.class, method);
        assertEquals("CASH_ON_DELIVERY", method.getType());
    }

    @Test
    void getPaymentMethod_CaseInsensitive() {
        PaymentMethod method1 = PaymentMethodFactory.getPaymentMethod("CREDIT_CARD");
        PaymentMethod method2 = PaymentMethodFactory.getPaymentMethod("Credit_Card");
        assertNotNull(method1);
        assertNotNull(method2);
        assertInstanceOf(CreditCardPayment.class, method1);
        assertInstanceOf(CreditCardPayment.class, method2);
    }

    @Test
    void getPaymentMethod_InvalidType_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> PaymentMethodFactory.getPaymentMethod("bitcoin"));
        assertTrue(exception.getMessage().contains("Metodo de pago no soportado"));
    }
}
