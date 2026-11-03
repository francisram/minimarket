package com.heladeria.api.repositories;

import com.heladeria.api.entities.ProductoSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoSimpleRepository extends JpaRepository<ProductoSimple, Long> {
    List<ProductoSimple> findByActivoTrue();
    List<ProductoSimple> findByCategoriaIgnoreCase(String categoria);
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<ProductoSimple> findByNombreIgnoreCase(String nombre);
    Optional<ProductoSimple> findByCodigo(String codigo);
    Optional<ProductoSimple> findByCodigoBarra(String codigoBarra);
    Optional<ProductoSimple> findByCodigoBarraIgnoreCase(String codigoBarra);
    boolean existsByCodigoBarraIgnoreCase(String codigoBarra);
    boolean existsByCodigoBarraIgnoreCaseAndIdNot(String codigoBarra, Long id);
    List<ProductoSimple> findByProveedorId(Long proveedorId);
    List<ProductoSimple> findByProveedorIdAndActivoTrue(Long proveedorId);

    /** Descuento atómico: devuelve 0 filas afectadas si el stock no alcanza (evita sobreventa concurrente). */
    @Modifying(flushAutomatically = true)
    @Query("update ProductoSimple p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

    /** Reintegro atómico (anulación de ventas). */
    @Modifying(flushAutomatically = true)
    @Query("update ProductoSimple p set p.stock = coalesce(p.stock, 0) + :cantidad where p.id = :id")
    int aumentarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

    /** Descuento atómico que no baja de cero (mermas manuales). */
    @Modifying(flushAutomatically = true)
    @Query("update ProductoSimple p set p.stock = case when p.stock >= :cantidad then p.stock - :cantidad else 0 end where p.id = :id")
    int reducirStockHastaCero(@Param("id") Long id, @Param("cantidad") int cantidad);
}
