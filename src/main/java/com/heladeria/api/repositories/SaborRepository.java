package com.heladeria.api.repositories;

import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.CategoriaSabor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaborRepository extends JpaRepository<Sabor, Long> {
    List<Sabor> findByDisponibleTrue();
    List<Sabor> findByCategoria(CategoriaSabor categoria);
    List<Sabor> findByAptoCeliacoTrue();
    List<Sabor> findByEsVeganoTrue();
    List<Sabor> findBySinAzucarTrue();
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Sabor> findByNombreIgnoreCase(String nombre);

    /** Descuento atómico: devuelve 0 filas afectadas si el stock no alcanza (evita sobreventa concurrente). */
    @Modifying(flushAutomatically = true)
    @Query("update Sabor s set s.stockKilos = s.stockKilos - :kilos where s.id = :id and s.stockKilos >= :kilos")
    int descontarStockKilos(@Param("id") Long id, @Param("kilos") double kilos);

    /** Reintegro atómico (anulación de ventas). */
    @Modifying(flushAutomatically = true)
    @Query("update Sabor s set s.stockKilos = s.stockKilos + :kilos where s.id = :id and s.stockKilos is not null")
    int aumentarStockKilos(@Param("id") Long id, @Param("kilos") double kilos);
}
