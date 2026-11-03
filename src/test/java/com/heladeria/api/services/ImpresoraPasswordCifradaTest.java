package com.heladeria.api.services;

import com.heladeria.api.config.SecretosCifradoMigracion;
import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.TipoConexion;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.security.CifradoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Contraseña de impresora cifrada en la base")
class ImpresoraPasswordCifradaTest {

    @Autowired private ImpresoraRepository repository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private SecretosCifradoMigracion migracion;

    private Impresora nueva(String password) {
        Impresora i = new Impresora();
        i.setNombre("Impresora " + UUID.randomUUID());
        i.setTipoConexion(TipoConexion.WINDOWS);
        i.setIp("192.168.1.50");
        i.setPuerto(445);
        i.setCola("TICKETS");
        i.setUsuario("caja");
        i.setPassword(password);
        return i;
    }

    private String columnaCruda(Long id) {
        return jdbc.queryForObject("SELECT password FROM impresoras WHERE id = ?", String.class, id);
    }

    @Test
    @DisplayName("En la base queda cifrada, y al leer la entidad se recupera el texto original")
    void seGuardaCifradaYSeLeeEnClaro() {
        Impresora guardada = repository.saveAndFlush(nueva("ClaveSecreta-123"));

        String cruda = columnaCruda(guardada.getId());
        assertTrue(cruda.startsWith(CifradoService.PREFIJO), cruda);
        assertFalse(cruda.contains("ClaveSecreta-123"));

        jdbc.execute("SELECT 1"); // fuerza que la lectura posterior pase por una consulta nueva
        assertEquals("ClaveSecreta-123", repository.findById(guardada.getId()).orElseThrow().getPassword());
    }

    @Test
    @DisplayName("Sin contraseña se guarda nulo")
    void sinPasswordQuedaNulo() {
        Impresora guardada = repository.saveAndFlush(nueva(null));
        assertNull(columnaCruda(guardada.getId()));
    }

    @Test
    @DisplayName("Al arrancar se cifran las contraseñas antiguas que estaban en texto plano")
    void migraTextoPlanoAntiguo() {
        Impresora guardada = repository.saveAndFlush(nueva(null));
        jdbc.update("UPDATE impresoras SET password = 'vieja-en-claro' WHERE id = ?", guardada.getId());
        assertEquals("vieja-en-claro", columnaCruda(guardada.getId()));

        assertTrue(migracion.cifrarPasswordsDeImpresoras() >= 1);

        String cruda = columnaCruda(guardada.getId());
        assertTrue(cruda.startsWith(CifradoService.PREFIJO));
        assertEquals(0, migracion.cifrarPasswordsDeImpresoras(), "Una segunda pasada no vuelve a cifrar");
    }
}
