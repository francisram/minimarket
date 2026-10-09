package com.heladeria.api.services;

import com.heladeria.api.dto.ToppingRequestDTO;
import com.heladeria.api.entities.Topping;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ToppingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ToppingService {

    private final ToppingRepository toppingRepository;

    public ToppingService(ToppingRepository toppingRepository) {
        this.toppingRepository = toppingRepository;
    }

    @Transactional(readOnly = true)
    public List<Topping> listar(boolean soloDisponibles) {
        if (soloDisponibles) {
            return toppingRepository.findByDisponibleTrue();
        }
        return toppingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Topping obtenerPorId(Long id) {
        return toppingRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Topping con id " + id + " no encontrado"));
    }

    @Transactional
    public Topping crear(ToppingRequestDTO dto) {
        if (toppingRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un topping con el nombre: " + dto.getNombre());
        }

        Topping topping = Topping.builder()
                .nombre(dto.getNombre())
                .precioExtra(dto.getPrecioExtra())
                .disponible(dto.getDisponible() == null || dto.getDisponible())
                .build();

        return toppingRepository.save(topping);
    }

    @Transactional
    public Topping actualizar(Long id, ToppingRequestDTO dto) {
        Topping topping = obtenerPorId(id);

        if (!topping.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                toppingRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un topping con el nombre: " + dto.getNombre());
        }

        topping.setNombre(dto.getNombre());
        topping.setPrecioExtra(dto.getPrecioExtra());
        if (dto.getDisponible() != null) {
            topping.setDisponible(dto.getDisponible());
        }

        return toppingRepository.save(topping);
    }

    @Transactional
    public Topping cambiarDisponibilidad(Long id, boolean disponible) {
        Topping topping = obtenerPorId(id);
        topping.setDisponible(disponible);
        return toppingRepository.save(topping);
    }

    @Transactional
    public void eliminar(Long id) {
        Topping topping = obtenerPorId(id);
        toppingRepository.delete(topping);
    }
}
