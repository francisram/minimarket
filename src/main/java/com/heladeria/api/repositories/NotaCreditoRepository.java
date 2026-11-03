package com.heladeria.api.repositories;

import com.heladeria.api.entities.NotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {

    List<NotaCredito> findByPedido_IdOrderByFechaAsc(Long pedidoId);

    List<NotaCredito> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);

    /** Cantidad ya acreditada de una línea de venta por notas de crédito anteriores. */
    @Query("select coalesce(sum(d.cantidad), 0) from DetalleNotaCredito d where d.detallePedido.id = :detalleId")
    long cantidadAcreditada(@Param("detalleId") Long detalleId);

    /** Importe ya acreditado de una línea de venta por notas de crédito anteriores. */
    @Query("select coalesce(sum(d.subtotal), 0) from DetalleNotaCredito d where d.detallePedido.id = :detalleId")
    java.math.BigDecimal montoAcreditado(@Param("detalleId") Long detalleId);
}
