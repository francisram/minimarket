package com.heladeria.api.repositories;

import com.heladeria.api.entities.PlanLicencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanLicenciaRepository extends JpaRepository<PlanLicencia, Long> {
    Optional<PlanLicencia> findByNombre(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
