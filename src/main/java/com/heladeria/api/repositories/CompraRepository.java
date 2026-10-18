package com.heladeria.api.repositories;

import com.heladeria.api.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findAllByOrderByFechaEmisionDescIdDesc();

    List<Compra> findByProveedorIdOrderByFechaEmisionDescIdDesc(Long proveedorId);
}
