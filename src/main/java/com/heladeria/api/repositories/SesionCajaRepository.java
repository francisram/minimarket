package com.heladeria.api.repositories;

import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    Optional<SesionCaja> findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja estado);

    boolean existsByEstado(EstadoSesionCaja estado);

    List<SesionCaja> findAllByOrderByFechaAperturaDesc();
}
