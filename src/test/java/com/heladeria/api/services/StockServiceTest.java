package com.heladeria.api.services;

import com.heladeria.api.dto.stock.AjusteStockDTO;
import com.heladeria.api.dto.stock.StockAlertasResponseDTO;
import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.repositories.PresentacionRepository;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import com.heladeria.api.repositories.SaborRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductoSimpleRepository productoSimpleRepository;

    @Autowired
    private SaborRepository saborRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Test
    @DisplayName("Debe detectar alertas de stock bajo y agotado para productos, sabores y envases")
    void testObtenerAlertasStock() {
        // Producto con stock bajo (3 <= 5)
        ProductoSimple agua = productoSimpleRepository.save(ProductoSimple.builder()
                .nombre("Agua Alerta Test")
                .precio(new BigDecimal("5000.00"))
                .stock(3)
                .stockMinimo(5)
                .activo(true)
                .build());

        // Sabor con stock bajo (1.5 <= 2.0)
        Sabor frutilla = saborRepository.save(Sabor.builder()
                .nombre("Frutilla Alerta Test")
                .categoria(CategoriaSabor.FRUTAL)
                .stockKilos(1.5)
                .stockMinimoKilos(2.0)
                .disponible(true)
                .build());

        // Presentación con stock agotado (0 <= 10)
        Presentacion cucurucho = presentacionRepository.save(Presentacion.builder()
                .nombre("Cucurucho Alerta Test")
                .precio(new BigDecimal("12000.00"))
                .maxSabores(1)
                .pesoGramosAprox(100)
                .stock(0)
                .stockMinimo(10)
                .activo(true)
                .build());

        StockAlertasResponseDTO alertas = stockService.obtenerAlertasStock();

        assertNotNull(alertas);
        assertTrue(alertas.getTotalAlertas() > 0);

        // Verificar que el producto figura en la lista
        boolean productoEncontrado = alertas.getProductosBajoStock().stream()
                .anyMatch(p -> p.getId().equals(agua.getId()) && "BAJO".equals(p.getEstadoAlerta()));
        assertTrue(productoEncontrado);

        // Verificar que el sabor figura en la lista
        boolean saborEncontrado = alertas.getSaboresBajoStock().stream()
                .anyMatch(s -> s.getId().equals(frutilla.getId()) && "BAJO".equals(s.getEstadoAlerta()));
        assertTrue(saborEncontrado);

        // Verificar que la presentación figura como AGOTADO
        boolean envaseEncontrado = alertas.getPresentacionesBajoStock().stream()
                .anyMatch(pr -> pr.getId().equals(cucurucho.getId()) && "AGOTADO".equals(pr.getEstadoAlerta()));
        assertTrue(envaseEncontrado);
    }

    @Test
    @DisplayName("Debe ajustar stock directamente y actualizar disponibilidad de sabor si stock > 0")
    void testAjusteStockDirecto() {
        Sabor dulceDeLeche = saborRepository.save(Sabor.builder()
                .nombre("DDL Ajuste Test")
                .categoria(CategoriaSabor.DULCE_DE_LECHE)
                .stockKilos(0.0)
                .stockMinimoKilos(2.0)
                .disponible(false)
                .build());

        AjusteStockDTO dto = new AjusteStockDTO();
        dto.setStockKilos(15.0);

        Sabor actualizado = stockService.actualizarStockSabor(dulceDeLeche.getId(), dto);

        assertEquals(15.0, actualizado.getStockKilos(), 0.001);
        assertTrue(actualizado.getDisponible(), "Al recargar stock positivo debe volver a estar disponible");
    }
}
