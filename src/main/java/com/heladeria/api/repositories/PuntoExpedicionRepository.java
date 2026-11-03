package com.heladeria.api.repositories;

import com.heladeria.api.entities.PuntoExpedicion;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuntoExpedicionRepository extends JpaRepository<PuntoExpedicion, Long> {

    List<PuntoExpedicion> findByActivoTrueOrderByEstablecimientoAscPuntoEmisionAsc();

    List<PuntoExpedicion> findAllByOrderByEstablecimientoAscPuntoEmisionAsc();

    Optional<PuntoExpedicion> findByEstablecimientoAndPuntoEmision(String establecimiento, String puntoEmision);

    boolean existsByEstablecimientoAndPuntoEmision(String establecimiento, String puntoEmision);

    boolean existsByEstablecimientoAndPuntoEmisionAndIdNot(String establecimiento, String puntoEmision, Long id);

    /**
     * Bloqueo pesimista de escritura (SELECT ... FOR UPDATE) para garantizar
     * atomicidad absoluta y evitar cualquier condición de carrera al incrementar números de factura.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // Si otra venta retiene el punto más de 5 s, falla rápido en vez de colgar al cajero
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT p FROM PuntoExpedicion p WHERE p.id = :id")
    Optional<PuntoExpedicion> findByIdForUpdate(@Param("id") Long id);
}
