package com.heladeria.api.repositories;

import com.heladeria.api.entities.Presentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresentacionRepository extends JpaRepository<Presentacion, Long> {
    List<Presentacion> findByActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Presentacion> findByNombreIgnoreCase(String nombre);
}
