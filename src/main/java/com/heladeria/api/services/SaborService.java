package com.heladeria.api.services;

import com.heladeria.api.dto.SaborRequestDTO;
import com.heladeria.api.entities.Producto;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.entities.enums.UnidadMedida;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ProductoRepository;
import com.heladeria.api.repositories.SaborRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaborService {

    private final SaborRepository saborRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    public SaborService(SaborRepository saborRepository,
                        ProductoRepository productoRepository,
                        AuditoriaService auditoriaService) {
        this.saborRepository = saborRepository;
        this.productoRepository = productoRepository;
        this.auditoriaService = auditoriaService;
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
                .stockMinimoKilos(dto.getStockMinimoKilos() != null ? dto.getStockMinimoKilos() : 2.0)
                .build();

        Sabor guardado = saborRepository.save(sabor);
        auditoriaService.registrar(
                "SABORES",
                "CREACION",
                guardado.getId(),
                guardado.getNombre(),
                "Creado sabor '" + guardado.getNombre() + "' (" + guardado.getCategoria() +
                "), Stock inicial: " + (guardado.getStockKilos() != null ? guardado.getStockKilos() : 0.0) + " kg"
        );
        sincronizarConCatalogoMaestro(guardado);
        return guardado;
    }

    @Transactional
    public Sabor actualizar(Long id, SaborRequestDTO dto) {
        Sabor sabor = obtenerPorId(id);

        if (!sabor.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                saborRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un sabor registrado con el nombre: " + dto.getNombre());
        }

        StringBuilder cambios = new StringBuilder();
        if (dto.getStockKilos() != null && !dto.getStockKilos().equals(sabor.getStockKilos())) {
            cambios.append("Stock: ").append(sabor.getStockKilos()).append(" kg ➔ ").append(dto.getStockKilos()).append(" kg; ");
        }
        if (dto.getDisponible() != null && !dto.getDisponible().equals(sabor.getDisponible())) {
            cambios.append("Disponibilidad: ").append(Boolean.TRUE.equals(sabor.getDisponible()) ? "Disponible" : "Agotado")
                   .append(" ➔ ").append(Boolean.TRUE.equals(dto.getDisponible()) ? "Disponible" : "Agotado").append("; ");
        }
        if (!sabor.getNombre().equalsIgnoreCase(dto.getNombre())) {
            cambios.append("Nombre: ").append(sabor.getNombre()).append(" ➔ ").append(dto.getNombre()).append("; ");
        }
        if (cambios.length() == 0) {
            cambios.append("Modificación de detalles del sabor.");
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
        if (dto.getStockMinimoKilos() != null) {
            sabor.setStockMinimoKilos(dto.getStockMinimoKilos());
        }

        Sabor actualizado = saborRepository.save(sabor);
        auditoriaService.registrar(
                "SABORES",
                "MODIFICACION",
                actualizado.getId(),
                actualizado.getNombre(),
                cambios.toString().trim()
        );
        sincronizarConCatalogoMaestro(actualizado);
        return actualizado;
    }

    @Transactional
    public Sabor cambiarDisponibilidad(Long id, boolean disponible) {
        Sabor sabor = obtenerPorId(id);
        sabor.setDisponible(disponible);
        Sabor actualizado = saborRepository.save(sabor);

        auditoriaService.registrar(
                "SABORES",
                "MODIFICACION",
                actualizado.getId(),
                actualizado.getNombre(),
                "Disponibilidad cambiada a: " + (disponible ? "Disponible para venta" : "Agotado en bacha")
        );
        sincronizarConCatalogoMaestro(actualizado);
        return actualizado;
    }

    @Transactional
    public void eliminar(Long id) {
        Sabor sabor = obtenerPorId(id);
        sincronizarEliminacionMaestro(sabor);
        saborRepository.delete(sabor);
        auditoriaService.registrar(
                "SABORES",
                "ELIMINACION",
                id,
                sabor.getNombre(),
                "Eliminado sabor: '" + sabor.getNombre() + "'"
        );
    }

    private void sincronizarConCatalogoMaestro(Sabor s) {
        if (s == null || s.getNombre() == null) {
            return;
        }
        try {
            Optional<Producto> opt = productoRepository.findByNombreIgnoreCase(s.getNombre().trim());
            Producto p = opt.orElseGet(Producto::new);
            p.setNombre(s.getNombre().trim());
            p.setDescripcion(s.getDescripcion());
            p.setTipoArticulo(TipoArticulo.SABOR);
            p.setCategoriaSabor(s.getCategoria());
            p.setAptoCeliaco(Boolean.TRUE.equals(s.getAptoCeliaco()));
            p.setEsVegano(Boolean.TRUE.equals(s.getEsVegano()));
            p.setSinAzucar(Boolean.TRUE.equals(s.getSinAzucar()));
            p.setActivo(Boolean.TRUE.equals(s.getDisponible()));
            if (s.getStockKilos() != null) {
                p.setStock(BigDecimal.valueOf(s.getStockKilos()));
            }
            if (s.getStockMinimoKilos() != null) {
                p.setStockMinimo(BigDecimal.valueOf(s.getStockMinimoKilos()));
            }
            if (p.getUnidadMedida() == null) {
                p.setUnidadMedida(UnidadMedida.KG);
            }
            if (p.getPrecioVenta() == null) {
                p.setPrecioVenta(BigDecimal.ZERO);
            }
            if (p.getPrecioCosto() == null) {
                p.setPrecioCosto(BigDecimal.ZERO);
            }
            productoRepository.save(p);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(SaborService.class)
                    .warn("Advertencia al sincronizar Sabor '{}' con Maestro ERP: {}", s.getNombre(), e.getMessage());
        }
    }

    private void sincronizarEliminacionMaestro(Sabor s) {
        if (s == null || s.getNombre() == null) {
            return;
        }
        try {
            productoRepository.findByNombreIgnoreCase(s.getNombre().trim())
                    .ifPresent(productoRepository::delete);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(SaborService.class)
                    .warn("No se pudo eliminar en Maestro ERP: {}", e.getMessage());
        }
    }
}
