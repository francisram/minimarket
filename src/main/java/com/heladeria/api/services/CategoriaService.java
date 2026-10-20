package com.heladeria.api.services;

import com.heladeria.api.dto.CategoriaDTO;
import com.heladeria.api.dto.CategoriaRequestDTO;
import com.heladeria.api.entities.Categoria;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final AuditoriaService auditoriaService;

    public CategoriaService(CategoriaRepository categoriaRepository, AuditoriaService auditoriaService) {
        this.categoriaRepository = categoriaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar(String filtro, TipoArticulo tipo, Boolean soloActivos) {
        String f = (filtro != null && !filtro.trim().isEmpty()) ? filtro.trim() : null;
        boolean activos = soloActivos != null && soloActivos;
        return categoriaRepository.buscarConFiltros(f, tipo, activos)
                .stream()
                .map(CategoriaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría con ID " + id + " no encontrada"));
        return new CategoriaDTO(cat);
    }

    @Transactional
    public CategoriaDTO crear(CategoriaRequestDTO dto) {
        String nombreLimpio = dto.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombreLimpio)) {
            throw new ReglaDeNegocioException("Ya existe una categoría registrada con el nombre: " + nombreLimpio);
        }

        Categoria cat = Categoria.builder()
                .nombre(nombreLimpio)
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .tipoArticulo(dto.getTipoArticulo())
                .activo(dto.getActivo() == null || dto.getActivo())
                .build();

        Categoria guardada = categoriaRepository.save(cat);

        auditoriaService.registrar(
                "CATEGORIAS",
                "CREACION",
                guardada.getId(),
                guardada.getNombre(),
                "Creada categoría '" + guardada.getNombre() + "'" + (guardada.getTipoArticulo() != null ? " para " + guardada.getTipoArticulo() : "")
        );

        return new CategoriaDTO(guardada);
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría con ID " + id + " no encontrada"));

        String nombreLimpio = dto.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombreLimpio, id)) {
            throw new ReglaDeNegocioException("Ya existe otra categoría registrada con el nombre: " + nombreLimpio);
        }

        StringBuilder cambios = new StringBuilder();
        if (!cat.getNombre().equalsIgnoreCase(nombreLimpio)) {
            cambios.append("Nombre: ").append(cat.getNombre()).append(" ➔ ").append(nombreLimpio).append("; ");
        }
        if (dto.getTipoArticulo() != cat.getTipoArticulo()) {
            cambios.append("Tipo: ").append(cat.getTipoArticulo()).append(" ➔ ").append(dto.getTipoArticulo()).append("; ");
        }
        if (dto.getActivo() != null && !dto.getActivo().equals(cat.getActivo())) {
            cambios.append("Estado: ").append(Boolean.TRUE.equals(cat.getActivo()) ? "Activa" : "Inactiva")
                   .append(" ➔ ").append(Boolean.TRUE.equals(dto.getActivo()) ? "Activa" : "Inactiva").append("; ");
        }
        if (cambios.length() == 0) {
            cambios.append("Modificación de detalles de la categoría.");
        }

        cat.setNombre(nombreLimpio);
        cat.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        cat.setTipoArticulo(dto.getTipoArticulo());
        if (dto.getActivo() != null) {
            cat.setActivo(dto.getActivo());
        }

        Categoria actualizada = categoriaRepository.save(cat);

        auditoriaService.registrar(
                "CATEGORIAS",
                "MODIFICACION",
                actualizada.getId(),
                actualizada.getNombre(),
                cambios.toString().trim()
        );

        return new CategoriaDTO(actualizada);
    }

    @Transactional
    public CategoriaDTO cambiarEstado(Long id, boolean activo) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría con ID " + id + " no encontrada"));

        cat.setActivo(activo);
        Categoria actualizada = categoriaRepository.save(cat);

        auditoriaService.registrar(
                "CATEGORIAS",
                "MODIFICACION",
                actualizada.getId(),
                actualizada.getNombre(),
                "Estado de categoría cambiado a: " + (activo ? "Activa" : "Inactiva")
        );

        return new CategoriaDTO(actualizada);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría con ID " + id + " no encontrada"));

        categoriaRepository.delete(cat);

        auditoriaService.registrar(
                "CATEGORIAS",
                "ELIMINACION",
                id,
                cat.getNombre(),
                "Eliminada categoría '" + cat.getNombre() + "'"
        );
    }
}
