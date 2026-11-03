package com.heladeria.api.services;

import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.entities.Producto;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.repositories.PresentacionRepository;
import com.heladeria.api.repositories.ProductoRepository;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import com.heladeria.api.repositories.SaborRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Mantiene alineados el stock del catálogo operativo (productos simples, envases, sabores) y el del
 * maestro de artículos (productos). El operativo es la fuente de verdad de las ventas; cada movimiento
 * se replica en el otro lado como un delta atómico, sin copiar valores absolutos que pisarían
 * lo que otra operación acaba de hacer.
 *
 * Nunca bloquea la operación principal: si no hay artículo vinculado, no hace nada.
 */
@Service
public class StockMaestroService {

    private final ProductoRepository productoRepository;
    private final ProductoSimpleRepository productoSimpleRepository;
    private final PresentacionRepository presentacionRepository;
    private final SaborRepository saborRepository;

    public StockMaestroService(ProductoRepository productoRepository,
                               ProductoSimpleRepository productoSimpleRepository,
                               PresentacionRepository presentacionRepository,
                               SaborRepository saborRepository) {
        this.productoRepository = productoRepository;
        this.productoSimpleRepository = productoSimpleRepository;
        this.presentacionRepository = presentacionRepository;
        this.saborRepository = saborRepository;
    }

    // ---------- operativo -> maestro ----------

    @Transactional(propagation = Propagation.MANDATORY)
    public void espejarProductoSimple(ProductoSimple ps, BigDecimal delta) {
        Optional<Producto> maestro = Optional.empty();
        if (ps.getCodigo() != null && !ps.getCodigo().isBlank()) {
            maestro = productoRepository.findBySkuIgnoreCase(ps.getCodigo().trim());
        }
        if (maestro.isEmpty() && ps.getCodigoBarra() != null && !ps.getCodigoBarra().isBlank()) {
            maestro = productoRepository.findByCodigoBarraIgnoreCase(ps.getCodigoBarra().trim());
        }
        if (maestro.isEmpty()) {
            maestro = productoRepository.findByNombreIgnoreCase(ps.getNombre().trim());
        }
        aplicar(maestro, delta);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void espejarPresentacion(Presentacion pres, BigDecimal delta) {
        aplicar(productoRepository.findByNombreIgnoreCase(pres.getNombre().trim())
                .filter(p -> p.getTipoArticulo() == TipoArticulo.ENVASE), delta);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void espejarSabor(Sabor sabor, BigDecimal delta) {
        aplicar(productoRepository.findByNombreIgnoreCase(sabor.getNombre().trim())
                .filter(p -> p.getTipoArticulo() == TipoArticulo.SABOR), delta);
    }

    private void aplicar(Optional<Producto> maestro, BigDecimal delta) {
        if (maestro.isEmpty() || maestro.get().getStock() == null || delta == null || delta.signum() == 0) {
            return;
        }
        Long id = maestro.get().getId();
        if (delta.signum() > 0) {
            productoRepository.aumentarStock(id, delta);
        } else {
            productoRepository.reducirStockHastaCero(id, delta.negate());
        }
    }

    // ---------- maestro -> operativo (compras) ----------

    /** Replica un ingreso o egreso hecho sobre el maestro en su registro operativo. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void aplicarEnOperativo(Producto maestro, BigDecimal delta) {
        if (maestro == null || maestro.getTipoArticulo() == null || delta == null || delta.signum() == 0 || maestro.getNombre() == null) {
            return;
        }
        String nombre = maestro.getNombre().trim();
        boolean sube = delta.signum() > 0;
        BigDecimal abs = delta.abs();
        int cant = abs.setScale(0, java.math.RoundingMode.HALF_UP).intValue();
        switch (maestro.getTipoArticulo()) {
            case ENVASE -> presentacionRepository.findByNombreIgnoreCase(nombre).ifPresent(p -> {
                if (p.getStock() == null) return;
                if (sube) presentacionRepository.aumentarStock(p.getId(), cant);
                else presentacionRepository.reducirStockHastaCero(p.getId(), cant);
            });
            case SABOR -> saborRepository.findByNombreIgnoreCase(nombre).ifPresent(s -> {
                if (s.getStockKilos() == null) return;
                if (sube) saborRepository.aumentarStockKilos(s.getId(), abs.doubleValue());
                else saborRepository.reducirStockKilosHastaCero(s.getId(), abs.doubleValue());
            });
            case VENTA_DIRECTA, ELABORADO, COMBO, SERVICIO -> {
                Optional<ProductoSimple> opt = productoSimpleRepository.findByNombreIgnoreCase(nombre);
                if (opt.isEmpty() && maestro.getSku() != null && !maestro.getSku().isBlank()) {
                    opt = productoSimpleRepository.findByCodigo(maestro.getSku().trim());
                }
                opt.ifPresent(ps -> {
                    if (ps.getStock() == null) return;
                    if (sube) productoSimpleRepository.aumentarStock(ps.getId(), cant);
                    else productoSimpleRepository.reducirStockHastaCero(ps.getId(), cant);
                });
            }
            default -> { }
        }
    }
}
