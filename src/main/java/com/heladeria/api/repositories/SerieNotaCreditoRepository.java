package com.heladeria.api.repositories;

import com.heladeria.api.entities.SerieNotaCredito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SerieNotaCreditoRepository extends JpaRepository<SerieNotaCredito, Long> {

    /** Bloqueo de fila: la numeración de las notas de crédito se toma una a la vez, como la de las facturas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("select s from SerieNotaCredito s where s.puntoExpedicionId = :id")
    Optional<SerieNotaCredito> findByIdForUpdate(@Param("id") Long id);
}
