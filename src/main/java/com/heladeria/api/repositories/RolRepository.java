package com.heladeria.api.repositories;

import com.heladeria.api.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombreRol(String nombreRol);
    boolean existsByNombreRolIgnoreCase(String nombreRol);
}
