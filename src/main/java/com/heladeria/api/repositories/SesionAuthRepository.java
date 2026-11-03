package com.heladeria.api.repositories;

import com.heladeria.api.entities.SesionAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SesionAuthRepository extends JpaRepository<SesionAuth, String> {

    @Query("select s from SesionAuth s join fetch s.usuario where s.refreshHash = :hash or s.refreshHashAnterior = :hash")
    Optional<SesionAuth> findByRefreshHashActualOAnterior(@Param("hash") String hash);

    /** Revoca todas las sesiones activas de un usuario (cambio de contraseña, cierre por administrador). */
    @Modifying(flushAutomatically = true)
    @Query("update SesionAuth s set s.revocadaEn = :ahora where s.usuario.id = :usuarioId and s.revocadaEn is null")
    int revocarTodasDelUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query("delete from SesionAuth s where s.expiraEn < :limite")
    int borrarExpiradasAntesDe(@Param("limite") LocalDateTime limite);
}
