package com.heladeria.api.services;

import com.heladeria.api.dto.LicenciaEstadoDTO;
import com.heladeria.api.entities.ControlReloj;
import com.heladeria.api.entities.Licencia;
import com.heladeria.api.entities.PlanLicencia;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.LicenciaRepository;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.PlanLicenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Estado informativo de la licencia y detección de retroceso del reloj (solo avisos, no bloquea). */
@SpringBootTest
@Transactional
@DisplayName("Licencia: estado de vencimiento y control de reloj")
class LicenciaRelojTest {

    @Autowired private ControlRelojService reloj;
    @Autowired private LicenciaService licenciaService;
    @Autowired private LicenciaRepository licenciaRepository;
    @Autowired private PlanLicenciaRepository planLicenciaRepository;
    @MockBean private PedidoRepository pedidoRepository;

    private final LocalDateTime base = LocalDateTime.now().withNano(0);

    @Test
    @DisplayName("Con el reloj avanzando normalmente no hay alerta y se actualiza la última fecha vista")
    void relojNormal() {
        reloj.registrar(base);
        ControlReloj c = reloj.registrar(base.plusHours(1));

        assertFalse(c.isRetrocesoDetectado());
        assertEquals(base.plusHours(1), c.getUltimaFechaVista());
    }

    @Test
    @DisplayName("Un ajuste pequeño (dentro de la tolerancia) no dispara la alerta")
    void ajusteDentroDeLaTolerancia() {
        reloj.registrar(base);
        ControlReloj c = reloj.registrar(base.minusMinutes(5));

        assertFalse(c.isRetrocesoDetectado());
    }

    @Test
    @DisplayName("Si el reloj retrocede más que la tolerancia se detecta y se conserva la fecha más alta")
    void retrocesoDetectado() {
        reloj.registrar(base);
        ControlReloj c = reloj.registrar(base.minusHours(2));

        assertTrue(c.isRetrocesoDetectado());
        assertEquals(120, c.getDesfaseMinutos());
        assertEquals(base, c.getUltimaFechaVista(), "No se retrocede la fecha vista");
        assertEquals(base, c.getReferenciaRetroceso());
    }

    @Test
    @DisplayName("El pedido más reciente también sirve de piso: no basta con borrar el control")
    void pisoPorUltimoPedido() {
        when(pedidoRepository.maxFechaCreacion()).thenReturn(base.plusDays(3));

        ControlReloj c = reloj.registrar(base);

        assertTrue(c.isRetrocesoDetectado());
        assertEquals(base.plusDays(3), c.getReferenciaRetroceso());
    }

    @Test
    @DisplayName("Reconocer la alerta exige que el reloj ya esté corregido")
    void reconocerSoloConRelojCorregido() {
        reloj.registrar(base);
        reloj.registrar(base.minusHours(2));

        assertThrows(ReglaDeNegocioException.class, () -> reloj.reconocerRetroceso("owner", base.minusHours(2)));
        assertTrue(reloj.obtener().isRetrocesoDetectado());

        reloj.reconocerRetroceso("owner", base.plusMinutes(1));
        assertFalse(reloj.obtener().isRetrocesoDetectado());
    }

    private void licenciaConVencimiento(LocalDate vencimiento) {
        PlanLicencia plan = planLicenciaRepository.findByNombre("Basico").orElseThrow();
        Licencia l = licenciaRepository.findById(1L).orElseGet(() -> new Licencia(1L, plan, null));
        l.setPlan(plan);
        l.setFechaVencimiento(vencimiento);
        licenciaRepository.saveAndFlush(l);
    }

    @Test
    @DisplayName("Estado de la licencia: vigente, por vencer, vencida y perpetua")
    void estadosDeLicencia() {
        LocalDate hoy = LocalDate.of(2026, 11, 3);

        licenciaConVencimiento(hoy.plusDays(60));
        assertEquals("VIGENTE", licenciaService.obtenerEstado(hoy).estado());

        licenciaConVencimiento(hoy.plusDays(10));
        LicenciaEstadoDTO porVencer = licenciaService.obtenerEstado(hoy);
        assertEquals("POR_VENCER", porVencer.estado());
        assertEquals(10, porVencer.diasRestantes());

        licenciaConVencimiento(hoy.minusDays(3));
        LicenciaEstadoDTO vencida = licenciaService.obtenerEstado(hoy);
        assertEquals("VENCIDA", vencida.estado());
        assertEquals(-3, vencida.diasRestantes());

        licenciaConVencimiento(null);
        assertEquals("PERPETUA", licenciaService.obtenerEstado(hoy).estado());
    }

    @Test
    @DisplayName("El estado incluye la alerta del reloj cuando fue atrasado")
    void estadoIncluyeAlertaDeReloj() {
        licenciaConVencimiento(null);
        reloj.registrar(base);
        reloj.registrar(base.minusHours(5));

        LicenciaEstadoDTO e = licenciaService.obtenerEstado(LocalDate.now());
        assertTrue(e.relojRetrocedido());
        assertNotNull(e.detalleReloj());
    }
}
