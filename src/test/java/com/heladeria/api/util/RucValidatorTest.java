package com.heladeria.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias del Validador RUC Módulo 11 (SET / DNIT Paraguay)")
class RucValidatorTest {

    @Test
    @DisplayName("calcularDigitoVerificador calcula correctamente personas físicas (ej. 3669922 -> 5)")
    void testCalcularDvPersonaFisica() {
        // C.I. 3.669.922 -> DV = 5
        assertEquals(5, RucValidator.calcularDigitoVerificador("3669922"));

        // Otros ejemplos conocidos
        assertEquals(1, RucValidator.calcularDigitoVerificador("5008432"));
        assertEquals(9, RucValidator.calcularDigitoVerificador("1234567"));
    }

    @Test
    @DisplayName("calcularDigitoVerificador calcula correctamente personas jurídicas (ej. 80106020 -> 6)")
    void testCalcularDvPersonaJuridica() {
        // RUC Serie 80 millones: 80106020 -> DV = 6
        assertEquals(6, RucValidator.calcularDigitoVerificador("80106020"));

        // 80123456 -> DV = 5
        assertEquals(5, RucValidator.calcularDigitoVerificador("80123456"));

        // 80012345 -> DV = 0 (resto 1 => 0)
        assertEquals(0, RucValidator.calcularDigitoVerificador("80012345"));
    }

    @Test
    @DisplayName("calcularDigitoVerificador calcula correctamente Consumidor Final (44444401 -> 7)")
    void testCalcularDvConsumidorFinal() {
        assertEquals(7, RucValidator.calcularDigitoVerificador("44444401"));
        assertEquals("44444401-7", RucValidator.RUC_CONSUMIDOR_FINAL);
    }

    @Test
    @DisplayName("calcularDigitoVerificador soporta alfanuméricos según resolución SET")
    void testCalcularDvAlfanumerico() {
        int dv = RucValidator.calcularDigitoVerificador("A123456");
        assertTrue(dv >= 0 && dv <= 9);
    }

    @Test
    @DisplayName("calcularDigitoVerificador retorna 0 para nulos o cadenas vacías")
    void testCalcularDvVacio() {
        assertEquals(0, RucValidator.calcularDigitoVerificador(null));
        assertEquals(0, RucValidator.calcularDigitoVerificador(""));
        assertEquals(0, RucValidator.calcularDigitoVerificador("   "));
    }

    @Test
    @DisplayName("esValido acepta RUCs legales válidos con formato BASE-DV")
    void testEsValidoRucCorrecto() {
        assertTrue(RucValidator.esValido("3669922-5"));
        assertTrue(RucValidator.esValido("80106020-6"));
        assertTrue(RucValidator.esValido("44444401-7"));
        assertTrue(RucValidator.esValido("80123456-5"));
        assertTrue(RucValidator.esValido("80012345-0"));
    }

    @Test
    @DisplayName("esValido rechaza RUCs con dígito verificador incorrecto")
    void testEsValidoDvIncorrecto() {
        assertFalse(RucValidator.esValido("3669922-0"));
        assertFalse(RucValidator.esValido("80106020-9"));
        assertFalse(RucValidator.esValido("44444401-1"));
        assertFalse(RucValidator.esValido("80123456-0"));
    }

    @Test
    @DisplayName("esValido rechaza formatos malformados")
    void testEsValidoFormatoInvalido() {
        assertFalse(RucValidator.esValido("80-106020-6"));
        assertFalse(RucValidator.esValido("80106020-X"));
        assertFalse(RucValidator.esValido("80106020--6"));
        assertFalse(RucValidator.esValido("80106020-6-1"));
        assertFalse(RucValidator.esValido(""));
        assertFalse(RucValidator.esValido(null));
    }

    @Test
    @DisplayName("esValido con permitirCedulaSinDv maneja cédulas de personas físicas sin guion")
    void testEsValidoPermitirCedulaSinDv() {
        // Con permitirCedulaSinDv = false, rechaza sin guion
        assertFalse(RucValidator.esValido("3669922", false));
        assertFalse(RucValidator.esValido("80106020", false));

        // Con permitirCedulaSinDv = true, acepta cédula civil alfanumérica
        assertTrue(RucValidator.esValido("3669922", true));
        assertTrue(RucValidator.esValido("80106020", true));

        // Sigue validando con rigor si se pasa con guion
        assertTrue(RucValidator.esValido("3669922-5", true));
        assertFalse(RucValidator.esValido("3669922-0", true));
    }

    @Test
    @DisplayName("autocompletarRuc genera o corrige el RUC con el DV correcto")
    void testAutocompletarRuc() {
        assertEquals("3669922-5", RucValidator.autocompletarRuc("3669922"));
        assertEquals("80106020-6", RucValidator.autocompletarRuc("80106020"));
        assertEquals("80106020-6", RucValidator.autocompletarRuc("80106020-0"));
        assertEquals("44444401-7", RucValidator.autocompletarRuc("44444401"));
        assertEquals("", RucValidator.autocompletarRuc(""));
        assertEquals("", RucValidator.autocompletarRuc(null));
    }
}
