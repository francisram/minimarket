package com.heladeria.api.repositories;

import com.heladeria.api.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findAllByOrderByFechaEmisionDesc();

    List<Compra> findByProveedorIdOrderByFechaEmisionDesc(Long proveedorId);

    @Query("SELECT c FROM Compra c " +
           "WHERE (:proveedorId IS NULL OR c.proveedor.id = :proveedorId) " +
           "  AND (:fechaDesde IS NULL OR c.fechaEmision >= :fechaDesde) " +
           "  AND (:fechaHasta IS NULL OR c.fechaEmision <= :fechaHasta) " +
           "  AND (:buscar IS NULL OR LOWER(c.numeroFactura) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
           "       OR LOWER(c.timbrado) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
           "       OR LOWER(c.proveedor.razonSocial) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
           "       OR LOWER(c.proveedor.ruc) LIKE LOWER(CONCAT('%', :buscar, '%'))) " +
           "ORDER BY c.fechaEmision DESC, c.id DESC")
    List<Compra> filtrarCompras(
            @Param("proveedorId") Long proveedorId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("buscar") String buscar
    );
}
