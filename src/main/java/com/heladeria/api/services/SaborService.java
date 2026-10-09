package com.heladeria.api.services;

import com.heladeria.api.dto.SaborRequestDTO;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.SaborRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaborService {

    private final SaborRepository saborRepository;

    public SaborService(SaborRepository saborRepository) {
        this.saborRepository = saborRepository;
    }

    @Transactional(readOnly = true)
    public List<Sabor> listar(Boolean soloDisponibles, CategoriaSabor categoria, Boolean celiaco, Boolean vegano, Boolean sinAzucar) {
        List<Sabor> sabores = saborRepository.findAll();

        return sabores.stream()
                .filter(s -> soloDisponibles == null || !soloDisponibles || Boolean.TRUE.equals(s.getDisponible()))
                .filter(s -> categoria == null || s.getCategoria() == categoria)
                .filter(s -> celiaco == null || !celiaco || Boolean.TRUE.equals(s.getAptoCeliaco()))
                .filter(s -> vegano == null || !vegano || Boolean.TRUE.equals(s.getEsVegano()))
                .filter(s -> sinAzucar == null || !sinAzucar || Boolean.TRUE.equals(s.getSinAzucar()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Sabor obtenerPorId(Long id) {
        return saborRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sabor con id " + id + " no encontrado"));
    }

    @Transactional
    public Sabor crear(SaborRequestDTO dto) {
        if (saborRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un sabor registrado con el nombre: " + dto.getNombre());
        }

        Sabor sabor = Sabor.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .categoria(dto.getCategoria())
                .aptoCeliaco(dto.getAptoCeliaco() != null && dto.getAptoCeliaco())
                .esVegano(dto.getEsVegano() != null && dto.getEsVegano())
                .sinAzucar(dto.getSinAzucar() != null && dto.getSinAzucar())
                .disponible(dto.getDisponible() == null || dto.getDisponible())
                .stockKilos(dto.getStockKilos())
                .build();

        return saborRepository.save(sabor);
    }

    @Transactional
    public Sabor actualizar(Long id, SaborRequestDTO dto) {
        Sabor sabor = obtenerPorId(id);

        if (!sabor.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                saborRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un sabor registrado con el nombre: " + dto.getNombre());
        }

        sabor.setNombre(dto.getNombre());
        sabor.setDescripcion(dto.getDescripcion());
        sabor.setCategoria(dto.getCategoria());
        sabor.setAptoCeliaco(dto.getAptoCeliaco() != null && dto.getAptoCeliaco());
        sabor.setEsVegano(dto.getEsVegano() != null && dto.getEsVegano());
        sabor.setSinAzucar(dto.getSinAzucar() != null && dto.getSinAzucar());
        if (dto.getDisponible() != null) {
            sabor.setDisponible(dto.getDisponible());
        }
        if (dto.getStockKilos() != null) {
            sabor.setStockKilos(dto.getStockKilos());
        }

        return saborRepository.save(sabor);
    }

    @Transactional
    public Sabor cambiarDisponibilidad(Long id, boolean disponible) {
        Sabor sabor = obtenerPorId(id);
        sabor.setDisponible(disponible);
        return saborRepository.save(sabor);
    }

    @Transactional
    public void eliminar(Long id) {
        Sabor sabor = obtenerPorId(id);
        saborRepository.delete(sabor);
    }
}
