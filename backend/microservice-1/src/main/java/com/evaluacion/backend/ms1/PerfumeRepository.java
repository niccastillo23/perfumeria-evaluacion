package com.evaluacion.backend.ms1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {
    List<Perfume> findByBrand(String brand);
    List<Perfume> findByPriceLessThan(Double price);
    List<Perfume> findByPriceBetween(Double min, Double max);
    List<Perfume> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Perfume p WHERE p.brand = :brand AND p.price <= :maxPrice")
    List<Perfume> findByBrandAndPriceLessThanEqual(@Param("brand") String brand, @Param("maxPrice") Double maxPrice);
}
