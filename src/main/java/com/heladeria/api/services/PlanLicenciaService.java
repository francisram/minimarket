package com.heladeria.api.services;

import com.heladeria.api.dto.PlanLicenciaDTO;
import com.heladeria.api.entities.Pagina;
import com.heladeria.api.entities.PlanLicencia;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.PaginaRepository;
import com.heladeria.api.repositories.PlanLicenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlanLicenciaService {

    private final PlanLicenciaRepository planLicenciaRepository;
    private final PaginaRepository paginaRepository;

    public PlanLicenciaService(PlanLicenciaRepository planLicenciaRepository, PaginaRepository paginaRepository) {
        this.planLicenciaRepository = planLicenciaRepository;
        this.paginaRepository = paginaRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanLicenciaDTO> listarTodos() {
        return planLicenciaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanLicenciaDTO obtenerPorId(Long id) {
        return planLicenciaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de licencia no encontrado con ID: " + id));
    }

    @Transactional
    public PlanLicenciaDTO guardar(PlanLicenciaDTO dto) {
        if (planLicenciaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new IllegalStateException("Ya existe un plan con el nombre: " + dto.getNombre());
        }

        PlanLicencia plan = new PlanLicencia();
        aplicarCampos(plan, dto);
        return toDTO(planLicenciaRepository.save(plan));
    }

    @Transactional
    public PlanLicenciaDTO actualizar(Long id, PlanLicenciaDTO dto) {
        PlanLicencia plan = planLicenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de licencia no encontrado con ID: " + id));

        if (!plan.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                planLicenciaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new IllegalStateException("Ya existe un plan con el nombre: " + dto.getNombre());
        }

        aplicarCampos(plan, dto);
        return toDTO(planLicenciaRepository.save(plan));
    }

    private void aplicarCampos(PlanLicencia plan, PlanLicenciaDTO dto) {
        plan.setNombre(dto.getNombre());

        Set<Pagina> paginas = new HashSet<>();
        if (dto.getPaginas() != null) {
            for (String clave : dto.getPaginas()) {
                Pagina pagina = paginaRepository.findByClave(clave)
                        .orElseThrow(() -> new IllegalStateException("Pagina desconocida en el catalogo: " + clave));
                paginas.add(pagina);
            }
        }
        plan.setPaginas(paginas);
    }

    public PlanLicenciaDTO toDTO(PlanLicencia pl) {
        PlanLicenciaDTO dto = new PlanLicenciaDTO();
        dto.setId(pl.getId());
        dto.setNombre(pl.getNombre());
        if (pl.getPaginas() != null) {
            dto.setPaginas(pl.getPaginas().stream()
                    .map(Pagina::getClave)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
