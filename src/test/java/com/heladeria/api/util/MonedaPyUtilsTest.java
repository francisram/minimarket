package com.heladeria.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias de utilidades de moneda paraguaya (MonedaPyUtils)")
class MonedaPyUtilsTest {

    @Test
    @DisplayName("redondearGs redondea a entero con HALF_UP")
    void testRedondearGs() {
        assertEquals(BigDecimal.ZERO, MonedaPyUtils.redondearGs(null));
        assertEquals(new BigDecimal("12000"), MonedaPyUtils.redondearGs(new BigDecimal("12000.00")));
        assertEquals(new BigDecimal("12000"), MonedaPyUtils.redondearGs(new BigDecimal("12000.49")));
        assertEquals(new BigDecimal("12001"), MonedaPyUtils.redondearGs(new BigDecimal("12000.50")));
        assertEquals(new BigDecimal("45000"), MonedaPyUtils.redondearGs(new BigDecimal("45000")));
    }

    @Test
    @DisplayName("formatearGs formatea con prefijo Gs. y separador de miles con punto, sin decimales ni símbolo unicode ₲")
    void testFormatearGs() {
        assertEquals("Gs. 0", MonedaPyUtils.formatearGs((BigDecimal) null));
        assertEquals("Gs. 12.000", MonedaPyUtils.formatearGs(new BigDecimal("12000")));
        assertEquals("Gs. 45.000", MonedaPyUtils.formatearGs(new BigDecimal("45000.00")));
        assertEquals("Gs. 1.250.000", MonedaPyUtils.formatearGs(new BigDecimal("1250000")));
        assertEquals("Gs. 80.000", MonedaPyUtils.formatearGs(80000L));

        // Verificar estrictamente que no contenga el símbolo unicode ₲ que rompe las impresoras térmicas
        String resultado = MonedaPyUtils.formatearGs(new BigDecimal("12000"));
        assertFalse(resultado.contains("\u20B2"), "Nunca debe contener el carácter unicode ₲");
        assertTrue(resultado.startsWith("Gs. "), "Debe comenzar con el prefijo 'Gs. '");
    }

    @Test
    @DisplayName("calcularIva10 liquida IVA 10% paraguayo (Total / 11) redondeado a entero")
    void testCalcularIva10() {
        assertEquals(BigDecimal.ZERO, MonedaPyUtils.calcularIva10(null));
        assertEquals(BigDecimal.ZERO, MonedaPyUtils.calcularIva10(BigDecimal.ZERO));

        // 11.000 / 11 = 1.000
        assertEquals(new BigDecimal("1000"), MonedaPyUtils.calcularIva10(new BigDecimal("11000")));

        // 12.000 / 11 = 1090.9090... -> redondea a 1091
        assertEquals(new BigDecimal("1091"), MonedaPyUtils.calcularIva10(new BigDecimal("12000")));

        // 22.000 / 11 = 2.000
        assertEquals(new BigDecimal("2000"), MonedaPyUtils.calcularIva10(new BigDecimal("22000")));
    }

    @Test
    @DisplayName("calcularGravada10 calcula base gravada (Total - IVA 10%)")
    void testCalcularGravada10() {
        assertEquals(BigDecimal.ZERO, MonedaPyUtils.calcularGravada10(null));

        // Para 11.000: IVA 10% = 1.000, Gravada 10% = 10.000
        assertEquals(new BigDecimal("10000"), MonedaPyUtils.calcularGravada10(new BigDecimal("11000")));

        // Para 12.000: IVA 10% = 1.091, Gravada 10% = 10.909
        assertEquals(new BigDecimal("10909"), MonedaPyUtils.calcularGravada10(new BigDecimal("12000")));
    }

    @Test
    @DisplayName("calcularIva5 liquida IVA 5% paraguayo (Total / 21) redondeado a entero")
    void testCalcularIva5() {
        assertEquals(BigDecimal.ZERO, MonedaPyUtils.calcularIva5(null));

        // 21.000 / 21 = 1.000
        assertEquals(new BigDecimal("1000"), MonedaPyUtils.calcularIva5(new BigDecimal("21000")));
    }
}
