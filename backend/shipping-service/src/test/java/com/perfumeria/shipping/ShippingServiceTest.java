package com.perfumeria.shipping;

import com.perfumeria.shipping.circuitbreaker.*;
import com.perfumeria.shipping.model.Shipment;
import com.perfumeria.shipping.repository.ShipmentRepository;
import com.perfumeria.shipping.service.ShippingService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private ShippingService shippingService;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowSize(10)
            .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        FedexCarrierService fedex = new FedexCarrierService();
        DhlCarrierService dhl = new DhlCarrierService();
        UpsCarrierService ups = new UpsCarrierService();
        CarrierFactory carrierFactory = new CarrierFactory(Arrays.asList(fedex, dhl, ups));

        shippingService = new ShippingService(shipmentRepository, carrierFactory, registry);
    }

    @Test
    void getAllShipments_ReturnsAllShipments() {
        List<Shipment> shipments = Arrays.asList(
            new Shipment("ORD-001", "FedEx", "FDX-001", "PENDING", "Addr1", 10.0),
            new Shipment("ORD-002", "DHL", "DHL-002", "SHIPPED", "Addr2", 15.0)
        );
        when(shipmentRepository.findAll()).thenReturn(shipments);

        List<Shipment> result = shippingService.getAllShipments();

        assertEquals(2, result.size());
        verify(shipmentRepository).findAll();
    }

    @Test
    void getShipmentsByOrderId_ReturnsMatchingShipments() {
        List<Shipment> shipments = Arrays.asList(
            new Shipment("ORD-001", "FedEx", "FDX-001", "PENDING", "Addr1", 10.0)
        );
        when(shipmentRepository.findByOrderId("ORD-001")).thenReturn(shipments);

        List<Shipment> result = shippingService.getShipmentsByOrderId("ORD-001");

        assertEquals(1, result.size());
        assertEquals("ORD-001", result.get(0).getOrderId());
        verify(shipmentRepository).findByOrderId("ORD-001");
    }

    @Test
    void getShipmentByTrackingNumber_Existing_ReturnsShipment() {
        Shipment shipment = new Shipment("ORD-001", "FedEx", "FDX-001", "PENDING", "Addr1", 10.0);
        when(shipmentRepository.findByTrackingNumber("FDX-001")).thenReturn(Optional.of(shipment));

        Optional<Shipment> result = shippingService.getShipmentByTrackingNumber("FDX-001");

        assertTrue(result.isPresent());
        assertEquals("FDX-001", result.get().getTrackingNumber());
    }

    @Test
    void getShipmentByTrackingNumber_NotExisting_ReturnsEmpty() {
        when(shipmentRepository.findByTrackingNumber("INVALID")).thenReturn(Optional.empty());

        Optional<Shipment> result = shippingService.getShipmentByTrackingNumber("INVALID");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateShipmentStatus_ExistingId_UpdatesStatus() {
        Shipment existing = new Shipment("ORD-001", "FedEx", "FDX-001", "PENDING", "Addr1", 10.0);
        existing.setId(1L);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = shippingService.updateShipmentStatus(1L, "SHIPPED");

        assertEquals("SHIPPED", result.getStatus());
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void updateShipmentStatus_NonExistingId_ThrowsException() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
            () -> shippingService.updateShipmentStatus(999L, "SHIPPED"));
        assertTrue(exception.getMessage().contains("Envio no encontrado"));
    }

    @Test
    void getShipmentsByStatus_ReturnsMatchingShipments() {
        List<Shipment> shipments = Arrays.asList(
            new Shipment("ORD-001", "FedEx", "FDX-001", "PENDING", "Addr1", 10.0),
            new Shipment("ORD-002", "DHL", "DHL-002", "PENDING", "Addr2", 15.0)
        );
        when(shipmentRepository.findByStatus("PENDING")).thenReturn(shipments);

        List<Shipment> result = shippingService.getShipmentsByStatus("PENDING");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> "PENDING".equals(s.getStatus())));
    }
}
