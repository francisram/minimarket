package com.heladeria.api.services;

import com.heladeria.api.dto.PresentacionRequestDTO;
import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PresentacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PresentacionService {

    private final PresentacionRepository presentacionRepository;

    public PresentacionService(PresentacionRepository presentacionRepository) {
        this.presentacionRepository = presentacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Presentacion> listar(boolean soloActivas) {
        if (soloActivas) {
            return presentacionRepository.findByActivoTrue();
        }
        return presentacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Presentacion obtenerPorId(Long id) {
        return presentacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presentación con id " + id + " no encontrada"));
    }

    @Transactional
    public Presentacion crear(PresentacionRequestDTO dto) {
        if (presentacionRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe una presentación con el nombre: " + dto.getNombre());
        }

        Presentacion presentacion = Presentacion.builder()
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .maxSabores(dto.getMaxSabores())
                .pesoGramosAprox(dto.getPesoGramosAprox())
                .activo(dto.getActivo() == null || dto.getActivo())
                .build();

        return presentacionRepository.save(presentacion);
    }

    @Transactional
    public Presentacion actualizar(Long id, PresentacionRequestDTO dto) {
        Presentacion presentacion = obtenerPorId(id);

        if (!presentacion.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                presentacionRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe una presentación con el nombre: " + dto.getNombre());
        }

        presentacion.setNombre(dto.getNombre());
        presentacion.setPrecio(dto.getPrecio());
        presentacion.setMaxSabores(dto.getMaxSabores());
        presentacion.setPesoGramosAprox(dto.getPesoGramosAprox());
        if (dto.getActivo() != null) {
            presentacion.setActivo(dto.getActivo());
        }

        return presentacionRepository.save(presentacion);
    }

    @Transactional
    public void eliminar(Long id) {
        Presentacion presentacion = obtenerPorId(id);
        presentacionRepository.delete(presentacion);
    }
}
