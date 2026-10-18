package com.heladeria.api.services;

import com.heladeria.api.dto.ProveedorDTO;
import com.heladeria.api.dto.ProveedorRequestDTO;
import com.heladeria.api.entities.Proveedor;
import com.heladeria.api.exceptions.ConflictException;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listar(String filtro, Boolean soloActivos) {
        List<Proveedor> proveedores;
        if (filtro != null && !filtro.trim().isEmpty()) {
            proveedores = proveedorRepository.buscarPorFiltro(filtro.trim());
        } else if (Boolean.TRUE.equals(soloActivos)) {
            proveedores = proveedorRepository.findByActivoTrueOrderByRazonSocialAsc();
        } else {
            proveedores = proveedorRepository.findAllByOrderByRazonSocialAsc();
        }
        return proveedores.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProveedorDTO> buscarPorRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) return Optional.empty();
        return proveedorRepository.findByRucIgnoreCase(ruc.trim()).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ProveedorDTO obtenerPorId(Long id) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con ID " + id + " no encontrado."));
        return toDTO(p);
    }

    @Transactional(readOnly = true)
    public Proveedor obtenerEntidadPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con ID " + id + " no encontrado."));
    }

    @Transactional
    public ProveedorDTO crear(ProveedorRequestDTO dto) {
        String rucLimpio = dto.getRuc().trim();
        if (proveedorRepository.existsByRucIgnoreCase(rucLimpio)) {
            throw new ConflictException("Ya existe un proveedor registrado con el RUC/C.I. " + rucLimpio);
        }
        Proveedor nuevo = new Proveedor();
        nuevo.setRuc(rucLimpio);
        nuevo.setRazonSocial(dto.getRazonSocial().trim());
        nuevo.setContacto(dto.getContacto() != null ? dto.getContacto().trim() : null);
        nuevo.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        nuevo.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        nuevo.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        nuevo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        nuevo.setFechaCreacion(LocalDateTime.now());
        return toDTO(proveedorRepository.save(nuevo));
    }

    @Transactional
    public ProveedorDTO actualizar(Long id, ProveedorRequestDTO dto) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con ID " + id + " no encontrado."));

        String rucLimpio = dto.getRuc().trim();
        if (!existente.getRuc().equalsIgnoreCase(rucLimpio) && proveedorRepository.existsByRucIgnoreCase(rucLimpio)) {
            throw new ConflictException("Ya existe otro proveedor con el RUC/C.I. " + rucLimpio);
        }

        existente.setRuc(rucLimpio);
        existente.setRazonSocial(dto.getRazonSocial().trim());
        existente.setContacto(dto.getContacto() != null ? dto.getContacto().trim() : null);
        existente.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        existente.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        existente.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        if (dto.getActivo() != null) existente.setActivo(dto.getActivo());
        existente.setFechaActualizacion(LocalDateTime.now());
        return toDTO(proveedorRepository.save(existente));
    }

    @Transactional
    public ProveedorDTO cambiarEstado(Long id, boolean activo) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con ID " + id + " no encontrado."));
        p.setActivo(activo);
        p.setFechaActualizacion(LocalDateTime.now());
        return toDTO(proveedorRepository.save(p));
    }

    public ProveedorDTO toDTO(Proveedor p) {
        return new ProveedorDTO(
                p.getId(),
                p.getRuc(),
                p.getRazonSocial(),
                p.getContacto(),
                p.getDireccion(),
                p.getTelefono(),
                p.getEmail(),
                p.getActivo(),
                p.getFechaCreacion(),
                p.getFechaActualizacion()
        );
    }
}
