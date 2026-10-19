package com.heladeria.api.services;

import com.heladeria.api.dto.PresentacionRequestDTO;
import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PresentacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PresentacionService {

    private final PresentacionRepository presentacionRepository;
    private final AuditoriaService auditoriaService;

    public PresentacionService(PresentacionRepository presentacionRepository, AuditoriaService auditoriaService) {
        this.presentacionRepository = presentacionRepository;
        this.auditoriaService = auditoriaService;
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
                .stock(dto.getStock())
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 10)
                .tipoIva(dto.getTipoIva() != null ? dto.getTipoIva() : TipoIva.IVA_10)
                .build();

        Presentacion guardada = presentacionRepository.save(presentacion);
        auditoriaService.registrar(
                "PRESENTACIONES_ENVASES",
                "CREACION",
                guardada.getId(),
                guardada.getNombre(),
                "Creada presentación/envase '" + guardada.getNombre() + "', Precio: Gs. " + guardada.getPrecio() +
                ", Stock: " + guardada.getStock() + ", Máx sabores: " + guardada.getMaxSabores()
        );
        return guardada;
    }

    @Transactional
    public Presentacion actualizar(Long id, PresentacionRequestDTO dto) {
        Presentacion presentacion = obtenerPorId(id);

        if (!presentacion.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                presentacionRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe una presentación con el nombre: " + dto.getNombre());
        }

        StringBuilder cambios = new StringBuilder();
        if (dto.getPrecio() != null && (presentacion.getPrecio() == null || dto.getPrecio().compareTo(presentacion.getPrecio()) != 0)) {
            cambios.append("Precio: Gs. ").append(presentacion.getPrecio()).append(" ➔ Gs. ").append(dto.getPrecio()).append("; ");
        }
        if (dto.getStock() != null && !dto.getStock().equals(presentacion.getStock())) {
            cambios.append("Stock: ").append(presentacion.getStock()).append(" ➔ ").append(dto.getStock()).append("; ");
        }
        if (!presentacion.getNombre().equalsIgnoreCase(dto.getNombre())) {
            cambios.append("Nombre: ").append(presentacion.getNombre()).append(" ➔ ").append(dto.getNombre()).append("; ");
        }
        if (dto.getActivo() != null && !dto.getActivo().equals(presentacion.getActivo())) {
            cambios.append("Estado: ").append(Boolean.TRUE.equals(presentacion.getActivo()) ? "Activo" : "Inactivo")
                   .append(" ➔ ").append(Boolean.TRUE.equals(dto.getActivo()) ? "Activo" : "Inactivo").append("; ");
        }
        if (cambios.length() == 0) {
            cambios.append("Modificación de datos generales del envase.");
        }

        presentacion.setNombre(dto.getNombre());
        presentacion.setPrecio(dto.getPrecio());
        presentacion.setMaxSabores(dto.getMaxSabores());
        presentacion.setPesoGramosAprox(dto.getPesoGramosAprox());
        if (dto.getActivo() != null) {
            presentacion.setActivo(dto.getActivo());
        }
        if (dto.getStock() != null) {
            presentacion.setStock(dto.getStock());
        }
        if (dto.getStockMinimo() != null) {
            presentacion.setStockMinimo(dto.getStockMinimo());
        }
        if (dto.getTipoIva() != null) {
            presentacion.setTipoIva(dto.getTipoIva());
        }

        Presentacion actualizada = presentacionRepository.save(presentacion);
        auditoriaService.registrar(
                "PRESENTACIONES_ENVASES",
                "MODIFICACION",
                actualizada.getId(),
                actualizada.getNombre(),
                cambios.toString().trim()
        );
        return actualizada;
    }

    @Transactional
    public void eliminar(Long id) {
        Presentacion presentacion = obtenerPorId(id);
        presentacionRepository.delete(presentacion);
        auditoriaService.registrar(
                "PRESENTACIONES_ENVASES",
                "ELIMINACION",
                id,
                presentacion.getNombre(),
                "Eliminada presentación/envase: '" + presentacion.getNombre() + "'"
        );
    }
}
