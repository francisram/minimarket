package com.heladeria.api.services;

import com.heladeria.api.dto.InstitucionDTO;
import com.heladeria.api.entities.Institucion;
import com.heladeria.api.repositories.InstitucionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Pruebas de InstitucionService y configuración de talonario")
class InstitucionServiceTest {

    @Autowired
    private InstitucionService institucionService;

    @Autowired
    private InstitucionRepository institucionRepository;

    @BeforeEach
    void setUp() {
        Institucion inst = institucionRepository.findById(1L).orElseGet(() -> {
            Institucion nueva = new Institucion();
            nueva.setId(1L);
            nueva.setNombre("Heladería Test");
            return institucionRepository.save(nueva);
        });
        inst.setTimbrado("99887766");
        inst.setTimbradoInicio(LocalDate.of(2026, 1, 1));
        inst.setTimbradoVencimiento(LocalDate.of(2026, 12, 31));
        inst.setNumeroInicioFactura(1L);
        inst.setNumeroFinFactura(500L);
        institucionRepository.save(inst);
    }

    @Test
    @DisplayName("get() debe retornar el DTO con los campos de talonario")
    void testGetInstitucion() {
        InstitucionDTO dto = institucionService.get();

        assertNotNull(dto);
        assertEquals("99887766", dto.getTimbrado());
        assertEquals(LocalDate.of(2026, 1, 1), dto.getTimbradoInicio());
        assertEquals(LocalDate.of(2026, 12, 31), dto.getTimbradoVencimiento());
        assertEquals(1L, dto.getNumeroInicioFactura());
        assertEquals(500L, dto.getNumeroFinFactura());
    }

    @Test
    @DisplayName("update() debe persistir timbradoInicio, numeroInicioFactura y numeroFinFactura")
    void testUpdateInstitucion() {
        InstitucionDTO request = new InstitucionDTO();
        request.setNombre("Heladería Renovada");
        request.setTimbrado("11223344");
        request.setTimbradoInicio(LocalDate.of(2026, 6, 1));
        request.setTimbradoVencimiento(LocalDate.of(2027, 6, 1));
        request.setNumeroInicioFactura(501L);
        request.setNumeroFinFactura(1500L);

        InstitucionDTO actualizado = institucionService.update(request);

        assertNotNull(actualizado);
        assertEquals("Heladería Renovada", actualizado.getNombre());
        assertEquals("11223344", actualizado.getTimbrado());
        assertEquals(LocalDate.of(2026, 6, 1), actualizado.getTimbradoInicio());
        assertEquals(LocalDate.of(2027, 6, 1), actualizado.getTimbradoVencimiento());
        assertEquals(501L, actualizado.getNumeroInicioFactura());
        assertEquals(1500L, actualizado.getNumeroFinFactura());

        Institucion entidad = institucionRepository.findById(1L).orElseThrow();
        assertEquals(501L, entidad.getNumeroInicioFactura());
        assertEquals(1500L, entidad.getNumeroFinFactura());
    }
}
