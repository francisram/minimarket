package com.heladeria.api.repositories;

import com.heladeria.api.entities.TomaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TomaInventarioRepository extends JpaRepository<TomaInventario, Long> {

    @Query("SELECT t FROM TomaInventario t ORDER BY t.fecha DESC")
    List<TomaInventario> findAllOrderByFechaDesc();
}
