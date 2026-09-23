package com.perfumeria.shipping.circuitbreaker;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class CarrierFactory {

    private final Map<String, Supplier<CarrierService>> carrierRegistry;

    public CarrierFactory(List<CarrierService> carrierServices) {
        this.carrierRegistry = Map.of(
            "fedex", () -> carrierServices.stream()
                .filter(c -> c instanceof FedexCarrierService)
                .findFirst().orElseThrow(),
            "dhl", () -> carrierServices.stream()
                .filter(c -> c instanceof DhlCarrierService)
                .findFirst().orElseThrow(),
            "ups", () -> carrierServices.stream()
                .filter(c -> c instanceof UpsCarrierService)
                .findFirst().orElseThrow()
        );
    }

    public CarrierService getCarrier(String carrierName) {
        Supplier<CarrierService> supplier = carrierRegistry.get(carrierName.toLowerCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Transportista no soportado: " + carrierName
                + ". Opciones disponibles: fedex, dhl, ups");
        }
        return supplier.get();
    }
}
