package com.heladeria.api.services;

import com.heladeria.api.dto.LicenciaDTO;
import com.heladeria.api.dto.LicenciaEstadoDTO;
import com.heladeria.api.entities.ControlReloj;
import com.heladeria.api.entities.Licencia;
import com.heladeria.api.entities.Pagina;
import com.heladeria.api.entities.PlanLicencia;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.LicenciaRepository;
import com.heladeria.api.repositories.PlanLicenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LicenciaService {

    private static final Long ID_UNICO = 1L;

    private final LicenciaRepository licenciaRepository;
    private static final long DIAS_AVISO_PREVIO = 15;

    private final PlanLicenciaRepository planLicenciaRepository;
    private final ControlRelojService controlRelojService;

    public LicenciaService(LicenciaRepository licenciaRepository, PlanLicenciaRepository planLicenciaRepository,
                           ControlRelojService controlRelojService) {
        this.licenciaRepository = licenciaRepository;
        this.planLicenciaRepository = planLicenciaRepository;
        this.controlRelojService = controlRelojService;
    }

    @Transactional(readOnly = true)
    public LicenciaEstadoDTO obtenerEstado() {
        return obtenerEstado(LocalDate.now());
    }

    /** Estado informativo de la licencia (no bloquea nada) más la alerta de retroceso de reloj. */
    @Transactional(readOnly = true)
    public LicenciaEstadoDTO obtenerEstado(LocalDate hoy) {
        ControlReloj reloj = controlRelojService.obtener();
        String detalleReloj = reloj.isRetrocesoDetectado()
                ? "El reloj del servidor se atrasó " + reloj.getDesfaseMinutos() + " min respecto de la última fecha registrada ("
                  + reloj.getReferenciaRetroceso() + "). Verifique la hora del servidor."
                : null;

        var licencia = licenciaRepository.findById(ID_UNICO);
        if (licencia.isEmpty() || licencia.get().getPlan() == null) {
            return new LicenciaEstadoDTO("SIN_LICENCIA", null, null, null, "No hay una licencia registrada en esta instalación.",
                    reloj.isRetrocesoDetectado(), detalleReloj, reloj.getUltimaFechaVista());
        }
        Licencia l = licencia.get();
        String plan = l.getPlan().getNombre();
        if (l.getFechaVencimiento() == null) {
            return new LicenciaEstadoDTO("PERPETUA", plan, null, null, "Licencia perpetua.",
                    reloj.isRetrocesoDetectado(), detalleReloj, reloj.getUltimaFechaVista());
        }
        long dias = ChronoUnit.DAYS.between(hoy, l.getFechaVencimiento());
        String estado;
        String mensaje;
        if (dias < 0) {
            estado = "VENCIDA";
            mensaje = "La licencia venció hace " + (-dias) + " día(s). Contacte al proveedor para renovarla.";
        } else if (dias <= DIAS_AVISO_PREVIO) {
            estado = "POR_VENCER";
            mensaje = dias == 0 ? "La licencia vence hoy." : "La licencia vence en " + dias + " día(s).";
        } else {
            estado = "VIGENTE";
            mensaje = "Licencia vigente hasta " + l.getFechaVencimiento() + ".";
        }
        return new LicenciaEstadoDTO(estado, plan, l.getFechaVencimiento(), dias, mensaje,
                reloj.isRetrocesoDetectado(), detalleReloj, reloj.getUltimaFechaVista());
    }

    @Transactional(readOnly = true)
    public LicenciaDTO obtener() {
        return licenciaRepository.findById(ID_UNICO)
                .map(this::toDTO)
                .orElseGet(LicenciaDTO::new);
    }

    @Transactional
    public LicenciaDTO actualizar(LicenciaDTO dto) {
        PlanLicencia plan = planLicenciaRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de licencia no encontrado con ID: " + dto.getPlanId()));

        Licencia licencia = licenciaRepository.findById(ID_UNICO).orElseGet(() -> new Licencia(ID_UNICO, null, null));
        licencia.setPlan(plan);
        licencia.setFechaVencimiento(dto.getFechaVencimiento());
        return toDTO(licenciaRepository.save(licencia));
    }

    // Regla critica de Hibernate: debe devolver una COPIA nueva, nunca la coleccion
    // gestionada de PlanLicencia.paginas, para evitar que Hibernate mueva el dueno
    // de la relacion ManyToMany y borre registros en plan_paginas.
    @Transactional(readOnly = true)
    public Set<Pagina> obtenerPaginasLicenciadas() {
        return licenciaRepository.findById(ID_UNICO)
                .map(l -> l.getPlan() != null && l.getPlan().getPaginas() != null
                        ? new HashSet<>(l.getPlan().getPaginas())
                        : new HashSet<Pagina>())
                .orElseGet(HashSet::new);
    }

    private LicenciaDTO toDTO(Licencia l) {
        LicenciaDTO dto = new LicenciaDTO();
        dto.setId(l.getId());
        if (l.getPlan() != null) {
            dto.setPlanId(l.getPlan().getId());
            dto.setPlanNombre(l.getPlan().getNombre());
            if (l.getPlan().getPaginas() != null) {
                dto.setPaginas(l.getPlan().getPaginas().stream()
                        .map(Pagina::getClave)
                        .collect(Collectors.toSet()));
            }
        }
        dto.setFechaVencimiento(l.getFechaVencimiento());
        return dto;
    }
}
