package com.heladeria.api.util;

/**
 * Validador oficial del Registro Único de Contribuyentes (RUC) de la República del Paraguay
 * según la especificación de la Dirección Nacional de Ingresos Tributarios (DNIT / ex SET).
 *
 * Utiliza el algoritmo oficial Módulo 11 con ponderadores cíclicos de 2 a 11 aplicados
 * de derecha a izquierda sobre la base alfanumérica (con conversión de letras a valor ASCII).
 */
public final class RucValidator {

    /**
     * RUC legal genérico para operaciones con "Consumidor Final" establecido por la SET / DNIT.
     */
    public static final String RUC_CONSUMIDOR_FINAL = "44444401-7";

    private static final int BASE_MAX = 11;

    private RucValidator() {
    }

    /**
     * Calcula el Dígito Verificador (DV) oficial según el algoritmo Módulo 11 de la DNIT.
     *
     * @param numeroBase Cadena alfanumérica sin guion ni dígito verificador.
     * @return Dígito verificador calculado (0 a 9).
     */
    public static int calcularDigitoVerificador(String numeroBase) {
        if (numeroBase == null || numeroBase.trim().isEmpty()) {
            return 0;
        }

        String limpio = numeroBase.trim().toUpperCase().replaceAll("[^0-9A-Z]", "");
        if (limpio.isEmpty()) {
            return 0;
        }

        int total = 0;
        int factor = 2;

        for (int i = limpio.length() - 1; i >= 0; i--) {
            char c = limpio.charAt(i);
            int valor;
            if (Character.isDigit(c)) {
                valor = Character.getNumericValue(c);
            } else {
                // Letras en RUCs antiguos/extranjeros se computan con su valor ASCII
                valor = (int) c;
            }

            total += valor * factor;
            factor++;
            if (factor > BASE_MAX) {
                factor = 2;
            }
        }

        int resto = total % 11;
        return (resto > 1) ? (11 - resto) : 0;
    }

    /**
     * Valida un RUC paraguayo en formato "BASE-DV" o como Cédula civil pura (si está habilitado).
     *
     * @param ruc Cadena con formato "BASE-DV" o sólo "BASE".
     * @param permitirCedulaSinDv Si es true, admite números de cédula sin guion para personas físicas.
     * @return true si es válido conforme a las reglas fiscales, false en caso contrario.
     */
    public static boolean esValido(String ruc, boolean permitirCedulaSinDv) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }

        String limpio = ruc.trim();

        if (limpio.contains("-")) {
            String[] partes = limpio.split("-");
            if (partes.length != 2) {
                return false;
            }

            String base = partes[0].trim();
            String dvStr = partes[1].trim();

            if (!base.matches("^[0-9A-Za-z]+$") || !dvStr.matches("^\\d$")) {
                return false;
            }

            int dvIngresado = Integer.parseInt(dvStr);
            int dvCalculado = calcularDigitoVerificador(base);

            return dvIngresado == dvCalculado;
        }

        if (permitirCedulaSinDv) {
            return limpio.matches("^[0-9A-Za-z]+$");
        }

        return false;
    }

    /**
     * Valida si un RUC paraguayo cumple con formato estricto "BASE-DV" y el Módulo 11.
     *
     * @param ruc Cadena en formato "BASE-DV".
     * @return true si el dígito verificador es matemáticamente correcto.
     */
    public static boolean esValido(String ruc) {
        return esValido(ruc, false);
    }

    /**
     * Autocompleta o corrige el RUC añadiéndole el Dígito Verificador correcto calculado.
     *
     * @param valor Número base o RUC con DV a corregir.
     * @return Cadena formateada como "BASE-DV".
     */
    public static String autocompletarRuc(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }

        String limpio = valor.trim();
        String base;
        if (limpio.contains("-")) {
            base = limpio.split("-")[0];
        } else {
            base = limpio;
        }

        String baseLimpia = base.replaceAll("[^0-9A-Za-z]", "");
        if (baseLimpia.isEmpty()) {
            return valor;
        }

        int dv = calcularDigitoVerificador(baseLimpia);
        return baseLimpia + "-" + dv;
    }
}
