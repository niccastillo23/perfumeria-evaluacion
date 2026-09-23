package com.evaluacion.backend.ms1;

import com.evaluacion.backend.ms1.service.PerfumeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class Ms1Application {

    private final PerfumeService perfumeService;

    public Ms1Application(PerfumeService perfumeService) {
        this.perfumeService = perfumeService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Ms1Application.class, args);
    }

    @GetMapping("/catalog")
    public List<Perfume> getCatalog() {
        return perfumeService.getAllPerfumes();
    }

    @GetMapping("/catalog/{id}")
    public Optional<Perfume> getPerfumeById(@PathVariable Long id) {
        return perfumeService.getPerfumeById(id);
    }

    @GetMapping("/catalog/brand/{brand}")
    public List<Perfume> getByBrand(@PathVariable String brand) {
        return perfumeService.getPerfumesByBrand(brand);
    }

    @GetMapping("/catalog/search")
    public List<Perfume> searchByName(@RequestParam String name) {
        return perfumeService.searchPerfumesByName(name);
    }

    @GetMapping("/catalog/price-range")
    public List<Perfume> getByPriceRange(@RequestParam Double min, @RequestParam Double max) {
        return perfumeService.getPerfumesByPriceRange(min, max);
    }

    @PostMapping("/catalog")
    public Perfume createPerfume(@RequestBody Perfume perfume) {
        return perfumeService.savePerfume(perfume);
    }

    @DeleteMapping("/catalog/{id}")
    public void deletePerfume(@PathVariable Long id) {
        perfumeService.deletePerfume(id);
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Catalog";
    }
}
