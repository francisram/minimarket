package com.heladeria.api.repositories;

import com.heladeria.api.entities.Categoria;
import com.heladeria.api.entities.enums.TipoArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Categoria> findByActivoTrue();

    List<Categoria> findByTipoArticulo(TipoArticulo tipoArticulo);

    List<Categoria> findByTipoArticuloAndActivoTrue(TipoArticulo tipoArticulo);

    @Query("SELECT c FROM Categoria c WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR (c.descripcion IS NOT NULL AND LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :filtro, '%')))) " +
           "AND (:tipo IS NULL OR c.tipoArticulo = :tipo) " +
           "AND (:soloActivos = false OR c.activo = true) " +
           "ORDER BY c.nombre ASC")
    List<Categoria> buscarConFiltros(@Param("filtro") String filtro,
                                    @Param("tipo") TipoArticulo tipo,
                                    @Param("soloActivos") boolean soloActivos);
}
