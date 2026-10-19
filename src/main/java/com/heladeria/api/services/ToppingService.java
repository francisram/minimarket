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
    private final AuditoriaService auditoriaService;

    public ToppingService(ToppingRepository toppingRepository, AuditoriaService auditoriaService) {
        this.toppingRepository = toppingRepository;
        this.auditoriaService = auditoriaService;
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

        Topping guardado = toppingRepository.save(topping);
        auditoriaService.registrar(
                "TOPPINGS",
                "CREACION",
                guardado.getId(),
                guardado.getNombre(),
                "Creado topping '" + guardado.getNombre() + "', Precio extra: Gs. " + guardado.getPrecioExtra()
        );
        return guardado;
    }

    @Transactional
    public Topping actualizar(Long id, ToppingRequestDTO dto) {
        Topping topping = obtenerPorId(id);

        if (!topping.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                toppingRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un topping con el nombre: " + dto.getNombre());
        }

        StringBuilder cambios = new StringBuilder();
        if (dto.getPrecioExtra() != null && (topping.getPrecioExtra() == null || dto.getPrecioExtra().compareTo(topping.getPrecioExtra()) != 0)) {
            cambios.append("Precio extra: Gs. ").append(topping.getPrecioExtra()).append(" ➔ Gs. ").append(dto.getPrecioExtra()).append("; ");
        }
        if (dto.getDisponible() != null && !dto.getDisponible().equals(topping.getDisponible())) {
            cambios.append("Disponibilidad: ").append(Boolean.TRUE.equals(topping.getDisponible()) ? "Disponible" : "No disponible")
                   .append(" ➔ ").append(Boolean.TRUE.equals(dto.getDisponible()) ? "Disponible" : "No disponible").append("; ");
        }
        if (!topping.getNombre().equalsIgnoreCase(dto.getNombre())) {
            cambios.append("Nombre: ").append(topping.getNombre()).append(" ➔ ").append(dto.getNombre()).append("; ");
        }
        if (cambios.length() == 0) {
            cambios.append("Modificación de detalles del topping.");
        }

        topping.setNombre(dto.getNombre());
        topping.setPrecioExtra(dto.getPrecioExtra());
        if (dto.getDisponible() != null) {
            topping.setDisponible(dto.getDisponible());
        }

        Topping actualizado = toppingRepository.save(topping);
        auditoriaService.registrar(
                "TOPPINGS",
                "MODIFICACION",
                actualizado.getId(),
                actualizado.getNombre(),
                cambios.toString().trim()
        );
        return actualizado;
    }

    @Transactional
    public Topping cambiarDisponibilidad(Long id, boolean disponible) {
        Topping topping = obtenerPorId(id);
        topping.setDisponible(disponible);
        Topping actualizado = toppingRepository.save(topping);

        auditoriaService.registrar(
                "TOPPINGS",
                "MODIFICACION",
                actualizado.getId(),
                actualizado.getNombre(),
                "Disponibilidad cambiada a: " + (disponible ? "Disponible" : "Agotado")
        );
        return actualizado;
    }

    @Transactional
    public void eliminar(Long id) {
        Topping topping = obtenerPorId(id);
        toppingRepository.delete(topping);
        auditoriaService.registrar(
                "TOPPINGS",
                "ELIMINACION",
                id,
                topping.getNombre(),
                "Eliminado topping: '" + topping.getNombre() + "'"
        );
    }
}
