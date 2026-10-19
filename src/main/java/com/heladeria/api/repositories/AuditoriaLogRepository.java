package com.heladeria.api.repositories;

import com.heladeria.api.entities.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    @Query("SELECT a FROM AuditoriaLog a WHERE " +
           "(:fechaDesde IS NULL OR a.fecha >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR a.fecha <= :fechaHasta) AND " +
           "(:usuario IS NULL OR LOWER(a.usuario) = LOWER(:usuario)) AND " +
           "(:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:accion IS NULL OR a.accion = :accion) AND " +
           "(:buscar IS NULL OR :buscar = '' OR " +
           " LOWER(a.detalle) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           " LOWER(a.entidadNombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           " LOWER(a.usuario) LIKE LOWER(CONCAT('%', :buscar, '%'))) " +
           "ORDER BY a.fecha DESC, a.id DESC")
    List<AuditoriaLog> filtrarLogs(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario,
            @Param("modulo") String modulo,
            @Param("accion") String accion,
            @Param("buscar") String buscar
    );

    @Query("SELECT a FROM AuditoriaLog a WHERE " +
           "(:fechaDesde IS NULL OR a.fecha >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR a.fecha <= :fechaHasta) AND " +
           "(:usuario IS NULL OR LOWER(a.usuario) = LOWER(:usuario)) AND " +
           "(:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:accion IS NULL OR a.accion = :accion) AND " +
           "(:buscar IS NULL OR :buscar = '' OR " +
           " LOWER(a.detalle) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           " LOWER(a.entidadNombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           " LOWER(a.usuario) LIKE LOWER(CONCAT('%', :buscar, '%')))")
    Page<AuditoriaLog> filtrarLogsPaginados(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario,
            @Param("modulo") String modulo,
            @Param("accion") String accion,
            @Param("buscar") String buscar,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a.usuario FROM AuditoriaLog a WHERE a.usuario IS NOT NULL ORDER BY a.usuario ASC")
    List<String> findDistinctUsuarios();

    @Query("SELECT DISTINCT a.modulo FROM AuditoriaLog a WHERE a.modulo IS NOT NULL ORDER BY a.modulo ASC")
    List<String> findDistinctModulos();

    @Query("SELECT DISTINCT a.accion FROM AuditoriaLog a WHERE a.accion IS NOT NULL ORDER BY a.accion ASC")
    List<String> findDistinctAcciones();
}
