package com.heladeria.api.repositories;

import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime inicio, LocalDateTime fin);
    List<Pedido> findAllByOrderByFechaCreacionDesc();
    List<Pedido> findBySesionCaja_Id(Long sesionCajaId);
    List<Pedido> findBySesionCaja_IdAndEstadoNot(Long sesionCajaId, EstadoPedido estado);
}
