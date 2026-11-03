package com.heladeria.api.dto;

import com.heladeria.api.entities.SerieNotaCredito;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class SerieNotaCreditoDTO {

    public Long puntoExpedicionId;
    @NotBlank(message = "El timbrado de notas de crédito es obligatorio")
    public String timbrado;
    public LocalDate timbradoInicio;
    public LocalDate timbradoVencimiento;
    public Long numeroInicio;
    public Long numeroFin;
    public Long ultimoNumero;

    public static SerieNotaCreditoDTO desde(SerieNotaCredito s) {
        SerieNotaCreditoDTO d = new SerieNotaCreditoDTO();
        d.puntoExpedicionId = s.getPuntoExpedicionId();
        d.timbrado = s.getTimbrado();
        d.timbradoInicio = s.getTimbradoInicio();
        d.timbradoVencimiento = s.getTimbradoVencimiento();
        d.numeroInicio = s.getNumeroInicio();
        d.numeroFin = s.getNumeroFin();
        d.ultimoNumero = s.getUltimoNumero();
        return d;
    }
}
