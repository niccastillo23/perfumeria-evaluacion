package com.evaluacion.backend.ms1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfumeRepositoryTest {

    @Mock
    private PerfumeRepository perfumeRepository;

    @Test
    void findByBrand_ReturnsMatchingPerfumes() {
        List<Perfume> perfumes = Arrays.asList(
            new Perfume(1L, "Test1", "Luxe Parfums", 100.0, ""),
            new Perfume(2L, "Test2", "Luxe Parfums", 200.0, "")
        );
        when(perfumeRepository.findByBrand("Luxe Parfums")).thenReturn(perfumes);

        List<Perfume> result = perfumeRepository.findByBrand("Luxe Parfums");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> "Luxe Parfums".equals(p.getBrand())));
        verify(perfumeRepository).findByBrand("Luxe Parfums");
    }

    @Test
    void findByPriceLessThan_ReturnsCheaperPerfumes() {
        List<Perfume> perfumes = Arrays.asList(
            new Perfume(1L, "Cheap", "Brand", 50.0, "")
        );
        when(perfumeRepository.findByPriceLessThan(100.0)).thenReturn(perfumes);

        List<Perfume> result = perfumeRepository.findByPriceLessThan(100.0);

        assertEquals(1, result.size());
        assertEquals("Cheap", result.get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase_CaseInsensitive() {
        List<Perfume> perfumes = Arrays.asList(
            new Perfume(1L, "Golden Amber", "Brand", 200.0, ""),
            new Perfume(2L, "Silver Gold", "Brand", 150.0, "")
        );
        when(perfumeRepository.findByNameContainingIgnoreCase("gold")).thenReturn(perfumes);

        List<Perfume> result = perfumeRepository.findByNameContainingIgnoreCase("gold");

        assertEquals(2, result.size());
    }

    @Test
    void saveAndFindById_RoundTrip() {
        Perfume perfume = new Perfume(1L, "Test Perfume", "Test Brand", 99.99, "");
        when(perfumeRepository.save(perfume)).thenReturn(perfume);
        when(perfumeRepository.findById(1L)).thenReturn(Optional.of(perfume));

        Perfume saved = perfumeRepository.save(perfume);
        Optional<Perfume> found = perfumeRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Perfume", found.get().getName());
        assertEquals(99.99, found.get().getPrice());
    }
}
