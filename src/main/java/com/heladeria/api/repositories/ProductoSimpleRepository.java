package com.heladeria.api.repositories;

import com.heladeria.api.entities.ProductoSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoSimpleRepository extends JpaRepository<ProductoSimple, Long> {
    List<ProductoSimple> findByActivoTrue();
    List<ProductoSimple> findByCategoriaIgnoreCase(String categoria);
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<ProductoSimple> findByNombreIgnoreCase(String nombre);
    Optional<ProductoSimple> findByCodigoBarra(String codigoBarra);
    Optional<ProductoSimple> findByCodigoBarraIgnoreCase(String codigoBarra);
    boolean existsByCodigoBarraIgnoreCase(String codigoBarra);
    boolean existsByCodigoBarraIgnoreCaseAndIdNot(String codigoBarra, Long id);
    List<ProductoSimple> findByProveedorId(Long proveedorId);
    List<ProductoSimple> findByProveedorIdAndActivoTrue(Long proveedorId);
}
