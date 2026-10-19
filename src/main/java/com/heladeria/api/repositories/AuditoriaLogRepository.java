package com.heladeria.api.repositories;

import com.heladeria.api.entities.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long>, JpaSpecificationExecutor<AuditoriaLog> {

    @Query("SELECT DISTINCT a.usuario FROM AuditoriaLog a WHERE a.usuario IS NOT NULL ORDER BY a.usuario ASC")
    List<String> findDistinctUsuarios();

    @Query("SELECT DISTINCT a.modulo FROM AuditoriaLog a WHERE a.modulo IS NOT NULL ORDER BY a.modulo ASC")
    List<String> findDistinctModulos();

    @Query("SELECT DISTINCT a.accion FROM AuditoriaLog a WHERE a.accion IS NOT NULL ORDER BY a.accion ASC")
    List<String> findDistinctAcciones();
}
