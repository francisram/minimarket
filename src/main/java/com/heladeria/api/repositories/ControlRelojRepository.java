package com.heladeria.api.repositories;

import com.heladeria.api.entities.ControlReloj;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlRelojRepository extends JpaRepository<ControlReloj, Long> {
}
