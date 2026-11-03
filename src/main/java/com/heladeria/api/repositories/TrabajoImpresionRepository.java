package com.heladeria.api.repositories;

import com.heladeria.api.entities.TrabajoImpresion;
import com.heladeria.api.entities.enums.EstadoTrabajoImpresion;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrabajoImpresionRepository extends JpaRepository<TrabajoImpresion, Long> {

    /**
     * Reclama trabajos listos para imprimir con SELECT ... FOR UPDATE SKIP LOCKED (lock.timeout = -2):
     * si hay varias instancias de la API, cada una toma trabajos distintos sin bloquearse ni duplicar.
     * Se excluyen las impresoras cuyo carril está ocupado (ver TrabajoImpresionService).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select t from TrabajoImpresion t where t.estado = :estado and t.proximoIntento <= :ahora "
            + "and (t.impresoraId is null or t.impresoraId not in :excluidas) "
            + "and (:incluirSinImpresora = true or t.impresoraId is not null) "
            + "order by t.proximoIntento, t.id")
    List<TrabajoImpresion> reclamar(@Param("estado") EstadoTrabajoImpresion estado,
                                    @Param("ahora") LocalDateTime ahora,
                                    @Param("excluidas") java.util.Collection<Long> excluidas,
                                    @Param("incluirSinImpresora") boolean incluirSinImpresora,
                                    Pageable pageable);

    List<TrabajoImpresion> findByEstadoAndFechaActualizacionBefore(EstadoTrabajoImpresion estado, LocalDateTime limite);

    List<TrabajoImpresion> findByEstadoOrderByFechaCreacionDesc(EstadoTrabajoImpresion estado);

    List<TrabajoImpresion> findTop100ByOrderByFechaCreacionDesc();

    long countByEstado(EstadoTrabajoImpresion estado);
}
