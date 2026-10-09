package com.heladeria.api.services;

import com.heladeria.api.dto.PaginaDTO;
import com.heladeria.api.entities.Pagina;
import com.heladeria.api.repositories.PaginaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaginaService {

    private final PaginaRepository paginaRepository;
    private final LicenciaService licenciaService;

    public PaginaService(PaginaRepository paginaRepository, LicenciaService licenciaService) {
        this.paginaRepository = paginaRepository;
        this.licenciaService = licenciaService;
    }

    @Transactional(readOnly = true)
    public List<PaginaDTO> listarTodas() {
        return paginaRepository.findAllByOrderByOrdenAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaginaDTO> listarDisponibles() {
        Set<String> clavesLicenciadas = licenciaService.obtenerPaginasLicenciadas().stream()
                .map(Pagina::getClave)
                .collect(Collectors.toSet());

        return paginaRepository.findAllByOrderByOrdenAsc().stream()
                .filter(p -> clavesLicenciadas.contains(p.getClave()))
                .sorted(Comparator.comparingInt(p -> p.getOrden() != null ? p.getOrden() : 0))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PaginaDTO toDTO(Pagina p) {
        return new PaginaDTO(
                p.getId(),
                p.getClave(),
                p.getNombre(),
                p.getUrl(),
                p.getIdPadre(),
                p.getIcono(),
                p.getOrden()
        );
    }
}
