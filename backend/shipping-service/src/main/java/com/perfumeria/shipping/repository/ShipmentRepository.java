package com.perfumeria.shipping.repository;

import com.perfumeria.shipping.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByOrderId(String orderId);
    List<Shipment> findByCarrier(String carrier);
    List<Shipment> findByStatus(String status);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
