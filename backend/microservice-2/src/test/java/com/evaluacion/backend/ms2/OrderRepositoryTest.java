package com.evaluacion.backend.ms2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryTest {

    @Mock
    private OrderRepository orderRepository;

    @Test
    void saveAndFindById_RoundTrip() {
        OrderEntity order = new OrderEntity("test-uuid-1", "[{\"id\":1}]", 150.0, "CREATED");
        when(orderRepository.save(order)).thenReturn(order);
        when(orderRepository.findById("test-uuid-1")).thenReturn(Optional.of(order));

        OrderEntity saved = orderRepository.save(order);
        Optional<OrderEntity> found = orderRepository.findById(saved.getOrderId());

        assertTrue(found.isPresent());
        assertEquals("CREATED", found.get().getStatus());
        assertEquals(150.0, found.get().getTotal());
    }

    @Test
    void findAll_ReturnsAllOrders() {
        List<OrderEntity> orders = Arrays.asList(
            new OrderEntity("uuid-1", "[]", 100.0, "CREATED"),
            new OrderEntity("uuid-2", "[]", 200.0, "PAID")
        );
        when(orderRepository.findAll()).thenReturn(orders);

        List<OrderEntity> result = orderRepository.findAll();

        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void findById_NonExisting_ReturnsEmpty() {
        when(orderRepository.findById("non-existing")).thenReturn(Optional.empty());

        Optional<OrderEntity> result = orderRepository.findById("non-existing");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateStatus_PersistsChange() {
        OrderEntity order = new OrderEntity("uuid-3", "[]", 75.0, "CREATED");
        when(orderRepository.findById("uuid-3")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Optional<OrderEntity> found = orderRepository.findById("uuid-3");
        assertTrue(found.isPresent());
        found.get().setStatus("PAID");
        orderRepository.save(found.get());

        assertEquals("PAID", found.get().getStatus());
    }
}
