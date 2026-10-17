package com.perfumeria.rabbitadmin.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record QueueRequest(
    @NotBlank
    @Pattern(regexp = "^(?!amq\\.)[A-Za-z0-9][A-Za-z0-9._-]{0,254}$", message = "El nombre de cola contiene caracteres inválidos o está reservado.")
    String name,
    @NotNull Boolean durable,
    @NotNull Boolean exclusive,
    @NotNull Boolean autoDelete,
    @Pattern(regexp = "^(?!amq\\.)[A-Za-z0-9][A-Za-z0-9._-]{0,254}$", message = "El exchange DLQ no es válido o está reservado.")
    String deadLetterExchange,
    @Pattern(regexp = "^[A-Za-z0-9*#._-]{1,255}$", message = "La routing key DLQ no es válida.")
    String deadLetterRoutingKey
) {
    @AssertTrue(message = "deadLetterExchange y deadLetterRoutingKey deben enviarse juntos.")
    public boolean isDeadLetterConfigurationComplete() {
        return (deadLetterExchange == null) == (deadLetterRoutingKey == null);
    }
}
