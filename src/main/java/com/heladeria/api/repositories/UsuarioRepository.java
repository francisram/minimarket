package com.heladeria.api.repositories;

import com.heladeria.api.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByRol_Id(Long rolId);
}
