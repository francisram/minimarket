package com.heladeria.api.repositories;

import com.heladeria.api.entities.Ticketera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketeraRepository extends JpaRepository<Ticketera, Long> {

    boolean existsByImpresoraId(Long impresoraId);
}
