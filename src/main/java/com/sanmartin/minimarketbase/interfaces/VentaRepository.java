package com.sanmartin.minimarketbase.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanmartin.minimarketbase.entities.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
}
