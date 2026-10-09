package com.heladeria.api.repositories;

import com.heladeria.api.entities.ProductoSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoSimpleRepository extends JpaRepository<ProductoSimple, Long> {
    List<ProductoSimple> findByActivoTrue();
    List<ProductoSimple> findByCategoriaIgnoreCase(String categoria);
    boolean existsByNombreIgnoreCase(String nombre);
}
