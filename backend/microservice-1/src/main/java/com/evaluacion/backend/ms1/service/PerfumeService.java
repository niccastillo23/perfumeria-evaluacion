package com.evaluacion.backend.ms1.service;

import com.evaluacion.backend.ms1.Perfume;
import com.evaluacion.backend.ms1.PerfumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerfumeService {

    private final PerfumeRepository perfumeRepository;

    public PerfumeService(PerfumeRepository perfumeRepository) {
        this.perfumeRepository = perfumeRepository;
    }

    public List<Perfume> getAllPerfumes() {
        return perfumeRepository.findAll();
    }

    public Optional<Perfume> getPerfumeById(Long id) {
        return perfumeRepository.findById(id);
    }

    public List<Perfume> getPerfumesByBrand(String brand) {
        return perfumeRepository.findByBrand(brand);
    }

    public List<Perfume> getPerfumesByPriceRange(Double min, Double max) {
        return perfumeRepository.findByPriceBetween(min, max);
    }

    public List<Perfume> searchPerfumesByName(String name) {
        return perfumeRepository.findByNameContainingIgnoreCase(name);
    }

    public Perfume savePerfume(Perfume perfume) {
        return perfumeRepository.save(perfume);
    }

    public void deletePerfume(Long id) {
        perfumeRepository.deleteById(id);
    }
}
