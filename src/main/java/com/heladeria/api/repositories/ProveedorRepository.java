package com.heladeria.api.repositories;

import com.heladeria.api.entities.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByRucIgnoreCase(String ruc);

    boolean existsByRucIgnoreCase(String ruc);

    List<Proveedor> findAllByOrderByRazonSocialAsc();

    List<Proveedor> findByActivoTrueOrderByRazonSocialAsc();

    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.ruc) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.contacto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "ORDER BY p.razonSocial ASC")
    List<Proveedor> buscarPorFiltro(@Param("filtro") String filtro);
}
