package com.heladeria.api.repositories;

import com.heladeria.api.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByRucIgnoreCase(String ruc);

    boolean existsByRucIgnoreCase(String ruc);

    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.ruc) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "ORDER BY c.razonSocial ASC")
    List<Cliente> buscarPorFiltro(@Param("filtro") String filtro);

    List<Cliente> findAllByOrderByRazonSocialAsc();
}
