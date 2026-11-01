package com.heladeria.api.repositories;

import com.heladeria.api.entities.MovimientoCaja;
import com.heladeria.api.entities.enums.TipoMovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findBySesionCaja_IdOrderByFechaDesc(Long sesionId);

    List<MovimientoCaja> findBySesionCaja_IdAndTipoOrderByFechaDesc(Long sesionId, TipoMovimientoCaja tipo);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.sesionCaja.id = :sesionId AND m.tipo = :tipo")
    BigDecimal sumMontoBySesionCajaIdAndTipo(@Param("sesionId") Long sesionId, @Param("tipo") TipoMovimientoCaja tipo);
}
