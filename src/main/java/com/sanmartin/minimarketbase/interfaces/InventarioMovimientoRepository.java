package com.sanmartin.minimarketbase.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanmartin.minimarketbase.entities.InventarioMovimiento;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Integer> {
}
