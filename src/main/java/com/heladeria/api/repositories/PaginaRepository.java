package com.heladeria.api.repositories;

import com.heladeria.api.entities.Pagina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaginaRepository extends JpaRepository<Pagina, Long> {
    Optional<Pagina> findByClave(String clave);
    List<Pagina> findAllByOrderByOrdenAsc();
    boolean existsByClave(String clave);
}
