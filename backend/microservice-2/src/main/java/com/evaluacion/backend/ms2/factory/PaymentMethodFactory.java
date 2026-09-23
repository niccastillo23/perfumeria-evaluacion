package com.evaluacion.backend.ms2.factory;

import java.util.Map;

public class PaymentMethodFactory {

    private static final Map<String, PaymentMethod> registry = Map.of(
        "credit_card", new CreditCardPayment(),
        "paypal", new PayPalPayment(),
        "cash_on_delivery", new CashOnDeliveryPayment()
    );

    public static PaymentMethod getPaymentMethod(String type) {
        PaymentMethod method = registry.get(type.toLowerCase());
        if (method == null) {
            throw new IllegalArgumentException(
                "Metodo de pago no soportado: " + type +
                ". Opciones disponibles: credit_card, paypal, cash_on_delivery"
            );
        }
        return method;
    }
}
