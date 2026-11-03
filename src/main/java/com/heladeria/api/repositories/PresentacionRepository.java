package com.heladeria.api.repositories;

import com.heladeria.api.entities.Presentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresentacionRepository extends JpaRepository<Presentacion, Long> {
    List<Presentacion> findByActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Presentacion> findByNombreIgnoreCase(String nombre);

    /** Descuento atómico: devuelve 0 filas afectadas si el stock no alcanza (evita sobreventa concurrente). */
    @Modifying(flushAutomatically = true)
    @Query("update Presentacion p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}
