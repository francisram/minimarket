package com.heladeria.api.repositories;

import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.CategoriaSabor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaborRepository extends JpaRepository<Sabor, Long> {
    List<Sabor> findByDisponibleTrue();
    List<Sabor> findByCategoria(CategoriaSabor categoria);
    List<Sabor> findByAptoCeliacoTrue();
    List<Sabor> findByEsVeganoTrue();
    List<Sabor> findBySinAzucarTrue();
    boolean existsByNombreIgnoreCase(String nombre);
}
