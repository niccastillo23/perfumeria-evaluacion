package com.perfumeria.rabbitadmin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
    @NotBlank
    @Pattern(regexp = "^(?!amq\\.)[A-Za-z0-9][A-Za-z0-9._-]{0,254}$", message = "El nombre de exchange no es válido o está reservado.")
    String name,
    @NotNull ExchangeType type,
    @NotNull Boolean durable,
    @NotNull Boolean autoDelete
) {
}
