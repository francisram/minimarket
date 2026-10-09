package com.heladeria.api.repositories;

import com.heladeria.api.entities.Impresora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImpresoraRepository extends JpaRepository<Impresora, Long> {

    Optional<Impresora> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}
