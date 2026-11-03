package com.heladeria.api.config;

import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Contraseñas de demostración: cambio obligatorio")
class ClavesPorDefectoCheckTest {

    @Autowired private ClavesPorDefectoCheck check;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario crear(String username, String clave) {
        Rol rol = rolRepository.findByNombreRol("CAJERO").orElseThrow();
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(clave));
        u.setRol(rol);
        u.setEstado(true);
        u.setPasswordNuncaExpira(true);
        u.setDebeCambiarPassword(false);
        return usuarioRepository.saveAndFlush(u);
    }

    @Test
    @DisplayName("Marca a quien conserva una clave de demostración y no toca a quien ya la cambió")
    void marcaSoloAUsuariosConClaveConocida() {
        Usuario conocida = crear("u_clave_conocida", "admin123");
        Usuario propia = crear("u_clave_propia", "Una-Clave-Larga-Propia-9!");

        int marcados = check.marcarUsuariosConClaveConocida();

        assertTrue(marcados >= 1);
        assertTrue(usuarioRepository.findById(conocida.getId()).orElseThrow().getDebeCambiarPassword());
        assertFalse(usuarioRepository.findById(propia.getId()).orElseThrow().getDebeCambiarPassword());
        assertEquals(0, check.marcarUsuariosConClaveConocida(), "Una segunda pasada no vuelve a marcar");
    }
}
