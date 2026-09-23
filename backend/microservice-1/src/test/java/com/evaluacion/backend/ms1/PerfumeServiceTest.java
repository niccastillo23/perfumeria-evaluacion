package com.evaluacion.backend.ms1;

import com.evaluacion.backend.ms1.service.PerfumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfumeServiceTest {

    @Mock
    private PerfumeRepository perfumeRepository;

    @InjectMocks
    private PerfumeService perfumeService;

    private Perfume testPerfume;

    @BeforeEach
    void setUp() {
        testPerfume = new Perfume(1L, "Elegance Gold", "Luxe Parfums", 189.99, "");
    }

    @Test
    void getAllPerfumes_ReturnsAllPerfumes() {
        List<Perfume> perfumes = Arrays.asList(
            testPerfume,
            new Perfume(2L, "Ocean Breeze", "Aqua Scents", 125.50, "")
        );
        when(perfumeRepository.findAll()).thenReturn(perfumes);

        List<Perfume> result = perfumeService.getAllPerfumes();

        assertEquals(2, result.size());
        verify(perfumeRepository).findAll();
    }

    @Test
    void getPerfumeById_ExistingId_ReturnsPerfume() {
        when(perfumeRepository.findById(1L)).thenReturn(Optional.of(testPerfume));

        Optional<Perfume> result = perfumeService.getPerfumeById(1L);

        assertTrue(result.isPresent());
        assertEquals("Elegance Gold", result.get().getName());
        verify(perfumeRepository).findById(1L);
    }

    @Test
    void getPerfumeById_NonExistingId_ReturnsEmpty() {
        when(perfumeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Perfume> result = perfumeService.getPerfumeById(999L);

        assertTrue(result.isEmpty());
        verify(perfumeRepository).findById(999L);
    }

    @Test
    void getPerfumesByBrand_ReturnsMatchingPerfumes() {
        List<Perfume> perfumes = Arrays.asList(
            testPerfume,
            new Perfume(2L, "Gold Intense", "Luxe Parfums", 210.00, "")
        );
        when(perfumeRepository.findByBrand("Luxe Parfums")).thenReturn(perfumes);

        List<Perfume> result = perfumeService.getPerfumesByBrand("Luxe Parfums");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> "Luxe Parfums".equals(p.getBrand())));
        verify(perfumeRepository).findByBrand("Luxe Parfums");
    }

    @Test
    void getPerfumesByPriceRange_ReturnsPerfumesInRange() {
        List<Perfume> perfumes = Arrays.asList(
            new Perfume(1L, "Cheap", "Brand", 50.0, ""),
            new Perfume(2L, "Mid", "Brand", 150.0, "")
        );
        when(perfumeRepository.findByPriceBetween(40.0, 160.0)).thenReturn(perfumes);

        List<Perfume> result = perfumeService.getPerfumesByPriceRange(40.0, 160.0);

        assertEquals(2, result.size());
        verify(perfumeRepository).findByPriceBetween(40.0, 160.0);
    }

    @Test
    void searchPerfumesByName_ReturnsMatchingResults() {
        List<Perfume> perfumes = Arrays.asList(
            new Perfume(1L, "Elegance Gold", "Luxe", 189.99, ""),
            new Perfume(2L, "Elegance Silver", "Luxe", 175.00, "")
        );
        when(perfumeRepository.findByNameContainingIgnoreCase("elegance")).thenReturn(perfumes);

        List<Perfume> result = perfumeService.searchPerfumesByName("elegance");

        assertEquals(2, result.size());
        verify(perfumeRepository).findByNameContainingIgnoreCase("elegance");
    }

    @Test
    void savePerfume_CallsRepositorySave() {
        when(perfumeRepository.save(any(Perfume.class))).thenReturn(testPerfume);

        Perfume result = perfumeService.savePerfume(testPerfume);

        assertNotNull(result);
        assertEquals("Elegance Gold", result.getName());
        verify(perfumeRepository).save(testPerfume);
    }

    @Test
    void deletePerfume_CallsRepositoryDeleteById() {
        perfumeService.deletePerfume(1L);
        verify(perfumeRepository).deleteById(1L);
    }
}
