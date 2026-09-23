package com.perfumeria.shipping;

import com.perfumeria.shipping.circuitbreaker.*;
import com.perfumeria.shipping.model.Shipment;
import com.perfumeria.shipping.repository.ShipmentRepository;
import com.perfumeria.shipping.service.ShippingService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private CarrierFactory carrierFactory;
    private ShippingService shippingService;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .waitDurationInOpenState(java.time.Duration.ofSeconds(30))
            .build();

        circuitBreakerRegistry = CircuitBreakerRegistry.of(config);

        FedexCarrierService fedex = new FedexCarrierService();
        DhlCarrierService dhl = new DhlCarrierService();
        UpsCarrierService ups = new UpsCarrierService();
        carrierFactory = new CarrierFactory(java.util.List.of(fedex, dhl, ups));

        shippingService = new ShippingService(shipmentRepository, carrierFactory, circuitBreakerRegistry);
    }

    @Test
    void circuitBreaker_InitialState_IsClosed() {
        String state = shippingService.getCircuitBreakerState();
        assertEquals("CLOSED", state);
    }

    @Test
    void createShipment_Fallback_WhenCarrierFails() {
        Shipment fallbackShipment = new Shipment(
            "ORD-001", "UNKNOWN", "FB-ORD-001-FALLBACK",
            "PENDING_FALLBACK", "Test Address", 0.0
        );
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(fallbackShipment);

        Shipment result = shippingService.createShipmentFallback(
            "ORD-001", "unknown", 100.0, "Test Address",
            new RuntimeException("Carrier API unavailable")
        );

        assertNotNull(result);
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("PENDING_FALLBACK", result.getStatus());
        assertEquals(0.0, result.getEstimatedCost());
        assertTrue(result.getTrackingNumber().contains("FALLBACK"));
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void createShipment_SuccessfulCarrierCall_ReturnsSavedShipment() {
        Shipment savedShipment = new Shipment(
            "ORD-002", "FedEx", "FDX-ORD-002-ABC123",
            "PENDING", "Test Address", 13.0
        );
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);

        Shipment result = shippingService.createShipment("ORD-002", "fedex", 100.0, "Test Address");

        assertNotNull(result);
        assertEquals("ORD-002", result.getOrderId());
        assertEquals("FedEx", result.getCarrier());
        assertEquals("PENDING", result.getStatus());
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void circuitBreaker_TransitionsToOpen_AfterFailures() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("testCB");

        for (int i = 0; i < 10; i++) {
            cb.onError(0, TimeUnit.MILLISECONDS, new RuntimeException("API failure"));
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }

    @Test
    void circuitBreaker_RemainsClosed_AfterSuccesses() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("testCB2");

        for (int i = 0; i < 10; i++) {
            cb.onSuccess(0, TimeUnit.MILLISECONDS);
        }

        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }
}
