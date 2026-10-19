package com.heladeria.api.repositories;

import com.heladeria.api.entities.Producto;
import com.heladeria.api.entities.enums.TipoArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByTipoArticulo(TipoArticulo tipoArticulo);

    List<Producto> findByTipoArticuloAndActivoTrue(TipoArticulo tipoArticulo);

    List<Producto> findByActivoTrue();

    Optional<Producto> findByCodigoBarraIgnoreCase(String codigoBarra);

    boolean existsByCodigoBarraIgnoreCase(String codigoBarra);

    boolean existsByCodigoBarraIgnoreCaseAndIdNot(String codigoBarra, Long id);

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Query("SELECT p FROM Producto p JOIN p.proveedores pp WHERE pp.proveedor.id = :proveedorId")
    List<Producto> findByProveedorId(@Param("proveedorId") Long proveedorId);

    @Query("SELECT p FROM Producto p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "(p.codigoBarra IS NOT NULL AND LOWER(p.codigoBarra) LIKE LOWER(CONCAT('%', :filtro, '%'))) OR " +
           "(p.sku IS NOT NULL AND LOWER(p.sku) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    List<Producto> buscarPorFiltro(@Param("filtro") String filtro);
}
