package com.heladeria.api.repositories;

import com.heladeria.api.entities.IntentoFallido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IntentoFallidoRepository extends JpaRepository<IntentoFallido, String> {

    /** Con bloqueo de fila: dos fallos simultáneos de la misma clave se cuentan uno detrás del otro. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IntentoFallido i where i.clave = :clave")
    Optional<IntentoFallido> findByClaveForUpdate(@Param("clave") String clave);
}
