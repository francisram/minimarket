package com.heladeria.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utilidades para manejo de moneda paraguaya (Guaraníes - PYG / Gs.).
 * En Paraguay no se utilizan decimales/centavos; los montos se redondean a enteros
 * y se formatean con separador de miles con punto (.) y prefijo "Gs. ".
 * IMPORTANTE: Nunca se debe emitir el carácter unicode ₲ (U+20B2) a buffers ESC/POS.
 */
public final class MonedaPyUtils {

    private static final DecimalFormatSymbols SIMBOLOS_PY;
    private static final String PATRON_ENTERO = "#,##0";

    static {
        SIMBOLOS_PY = new DecimalFormatSymbols(new Locale("es", "PY"));
        SIMBOLOS_PY.setGroupingSeparator('.');
    }

    private MonedaPyUtils() {
    }

    /**
     * Redondea un importe BigDecimal a entero (escala 0) usando RoundingMode.HALF_UP.
     */
    public static BigDecimal redondearGs(BigDecimal monto) {
        if (monto == null) {
            return BigDecimal.ZERO;
        }
        return monto.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Formatea un importe en Guaraníes con prefijo "Gs. " y separador de miles con punto.
     * Ejemplo: 12000 -> "Gs. 12.000"
     */
    public static String formatearGs(BigDecimal monto) {
        if (monto == null) {
            return "Gs. 0";
        }
        DecimalFormat df = new DecimalFormat(PATRON_ENTERO, SIMBOLOS_PY);
        return "Gs. " + df.format(redondearGs(monto));
    }

    /**
     * Formatea un valor numérico primitivo en Guaraníes.
     */
    public static String formatearGs(long monto) {
        DecimalFormat df = new DecimalFormat(PATRON_ENTERO, SIMBOLOS_PY);
        return "Gs. " + df.format(monto);
    }

    /**
     * Calcula la porción de IVA 10% incluida en el total: Total / 11 redondeado a entero.
     */
    public static BigDecimal calcularIva10(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(11), 0, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la base gravada 10% (Total - IVA 10%).
     */
    public static BigDecimal calcularGravada10(BigDecimal total) {
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return redondearGs(total).subtract(calcularIva10(total));
    }

    /**
     * Calcula la porción de IVA 5% incluida en el total: Total / 21 redondeado a entero.
     */
    public static BigDecimal calcularIva5(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(21), 0, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la base gravada 5% (Total - IVA 5%).
     */
    public static BigDecimal calcularGravada5(BigDecimal total) {
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return redondearGs(total).subtract(calcularIva5(total));
    }
}
