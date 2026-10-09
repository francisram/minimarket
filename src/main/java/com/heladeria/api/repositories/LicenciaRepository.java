package com.heladeria.api.repositories;

import com.heladeria.api.entities.Licencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenciaRepository extends JpaRepository<Licencia, Long> {
}
