package com.heladeria.api.services;

import com.heladeria.api.dto.LicenciaDTO;
import com.heladeria.api.entities.Licencia;
import com.heladeria.api.entities.Pagina;
import com.heladeria.api.entities.PlanLicencia;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.LicenciaRepository;
import com.heladeria.api.repositories.PlanLicenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LicenciaService {

    private static final Long ID_UNICO = 1L;

    private final LicenciaRepository licenciaRepository;
    private final PlanLicenciaRepository planLicenciaRepository;

    public LicenciaService(LicenciaRepository licenciaRepository, PlanLicenciaRepository planLicenciaRepository) {
        this.licenciaRepository = licenciaRepository;
        this.planLicenciaRepository = planLicenciaRepository;
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
