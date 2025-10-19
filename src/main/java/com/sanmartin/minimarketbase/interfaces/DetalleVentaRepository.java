package com.sanmartin.minimarketbase.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanmartin.minimarketbase.entities.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
}
