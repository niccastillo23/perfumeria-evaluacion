package com.perfumeria.rabbitadmin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BindingRequest(
    @NotBlank
    @Pattern(regexp = "^(?!amq\\.)[A-Za-z0-9][A-Za-z0-9._-]{0,254}$", message = "El nombre de cola no es válido o está reservado.")
    String queueName,
    @NotBlank
    @Pattern(regexp = "^(?!amq\\.)[A-Za-z0-9][A-Za-z0-9._-]{0,254}$", message = "El nombre de exchange no es válido o está reservado.")
    String exchangeName,
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9*#._-]{1,255}$", message = "La routing key no es válida.")
    String routingKey
) {
}
