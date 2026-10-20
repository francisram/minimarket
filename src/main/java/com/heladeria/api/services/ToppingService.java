package com.heladeria.api.services;

import com.heladeria.api.dto.ToppingRequestDTO;
import com.heladeria.api.entities.Producto;
import com.heladeria.api.entities.Topping;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.entities.enums.UnidadMedida;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ProductoRepository;
import com.heladeria.api.repositories.ToppingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ToppingService {

    private final ToppingRepository toppingRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    public ToppingService(ToppingRepository toppingRepository,
                          ProductoRepository productoRepository,
                          AuditoriaService auditoriaService) {
        this.toppingRepository = toppingRepository;
        this.productoRepository = productoRepository;
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
        sincronizarConCatalogoMaestro(guardado);
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
        sincronizarConCatalogoMaestro(actualizado);
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
        sincronizarConCatalogoMaestro(actualizado);
        return actualizado;
    }

    @Transactional
    public void eliminar(Long id) {
        Topping topping = obtenerPorId(id);
        sincronizarEliminacionMaestro(topping);
        toppingRepository.delete(topping);
        auditoriaService.registrar(
                "TOPPINGS",
                "ELIMINACION",
                id,
                topping.getNombre(),
                "Eliminado topping: '" + topping.getNombre() + "'"
        );
    }

    private void sincronizarConCatalogoMaestro(Topping top) {
        if (top == null || top.getNombre() == null) {
            return;
        }
        try {
            Optional<Producto> opt = productoRepository.findByNombreIgnoreCase(top.getNombre().trim());
            Producto p = opt.orElseGet(Producto::new);
            p.setNombre(top.getNombre().trim());
            p.setTipoArticulo(TipoArticulo.TOPPING);
            p.setPrecioVenta(top.getPrecioExtra() != null ? top.getPrecioExtra() : BigDecimal.ZERO);
            p.setActivo(Boolean.TRUE.equals(top.getDisponible()));
            if (p.getUnidadMedida() == null) {
                p.setUnidadMedida(UnidadMedida.UNIDAD);
            }
            if (p.getStock() == null) {
                p.setStock(BigDecimal.ZERO);
            }
            if (p.getStockMinimo() == null) {
                p.setStockMinimo(BigDecimal.ZERO);
            }
            if (p.getPrecioCosto() == null) {
                p.setPrecioCosto(BigDecimal.ZERO);
            }
            if ((p.getSku() == null || p.getSku().isBlank()) && top.getId() != null) {
                p.setSku("T-" + top.getId());
            }
            productoRepository.save(p);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ToppingService.class)
                    .warn("Advertencia al sincronizar Topping '{}' con Maestro ERP: {}", top.getNombre(), e.getMessage());
        }
    }

    private void sincronizarEliminacionMaestro(Topping top) {
        if (top == null || top.getNombre() == null) {
            return;
        }
        try {
            productoRepository.findByNombreIgnoreCase(top.getNombre().trim())
                    .ifPresent(productoRepository::delete);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ToppingService.class)
                    .warn("No se pudo eliminar Topping en Maestro ERP: {}", e.getMessage());
        }
    }
}
