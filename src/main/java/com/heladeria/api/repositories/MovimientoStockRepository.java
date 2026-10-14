package com.heladeria.api.repositories;

import com.heladeria.api.entities.MovimientoStock;
import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT m FROM MovimientoStock m WHERE " +
           "m.fecha >= :inicio AND m.fecha <= :fin AND " +
           "(:tipoItem IS NULL OR m.tipoItem = :tipoItem) AND " +
           "(:tipoMovimiento IS NULL OR m.tipoMovimiento = :tipoMovimiento) AND " +
           "(:itemId IS NULL OR m.itemId = :itemId) " +
           "ORDER BY m.fecha DESC")
    List<MovimientoStock> filtrarMovimientos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoItem") TipoItemStock tipoItem,
            @Param("tipoMovimiento") TipoMovimientoStock tipoMovimiento,
            @Param("itemId") Long itemId
    );
}
