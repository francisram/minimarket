package com.heladeria.api.repositories;

import com.heladeria.api.entities.DetalleTomaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleTomaInventarioRepository extends JpaRepository<DetalleTomaInventario, Long> {

    List<DetalleTomaInventario> findByTomaInventarioIdOrderByIdAsc(Long tomaInventarioId);
}
