package com.heladeria.api.repositories;

import com.heladeria.api.entities.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {

    List<ProductoProveedor> findByProductoId(Long productoId);

    List<ProductoProveedor> findByProveedorId(Long proveedorId);

    Optional<ProductoProveedor> findByProductoIdAndProveedorId(Long productoId, Long proveedorId);

    void deleteByProductoIdAndProveedorId(Long productoId, Long proveedorId);
}
