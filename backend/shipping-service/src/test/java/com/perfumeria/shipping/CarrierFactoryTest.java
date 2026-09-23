package com.perfumeria.shipping;

import com.perfumeria.shipping.circuitbreaker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CarrierFactoryTest {

    private CarrierFactory carrierFactory;
    private FedexCarrierService fedex;
    private DhlCarrierService dhl;
    private UpsCarrierService ups;

    @BeforeEach
    void setUp() {
        fedex = new FedexCarrierService();
        dhl = new DhlCarrierService();
        ups = new UpsCarrierService();
        carrierFactory = new CarrierFactory(List.of(fedex, dhl, ups));
    }

    @Test
    void getCarrier_Fedex_ReturnsFedexService() {
        CarrierService service = carrierFactory.getCarrier("fedex");
        assertInstanceOf(FedexCarrierService.class, service);
        assertEquals("FedEx", service.getCarrierName());
    }

    @Test
    void getCarrier_Dhl_ReturnsDhlService() {
        CarrierService service = carrierFactory.getCarrier("dhl");
        assertInstanceOf(DhlCarrierService.class, service);
        assertEquals("DHL", service.getCarrierName());
    }

    @Test
    void getCarrier_Ups_ReturnsUpsService() {
        CarrierService service = carrierFactory.getCarrier("ups");
        assertInstanceOf(UpsCarrierService.class, service);
        assertEquals("UPS", service.getCarrierName());
    }

    @Test
    void getCarrier_CaseInsensitive() {
        CarrierService s1 = carrierFactory.getCarrier("FEDEX");
        CarrierService s2 = carrierFactory.getCarrier("FedEx");
        assertNotNull(s1);
        assertNotNull(s2);
        assertInstanceOf(FedexCarrierService.class, s1);
        assertInstanceOf(FedexCarrierService.class, s2);
    }

    @Test
    void getCarrier_InvalidCarrier_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> carrierFactory.getCarrier("unknown"));
        assertTrue(exception.getMessage().contains("Transportista no soportado"));
    }
}
