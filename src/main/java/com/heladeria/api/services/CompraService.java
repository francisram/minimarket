package com.heladeria.api.services;

import com.heladeria.api.dto.compras.CompraDTO;
import com.heladeria.api.dto.compras.RegistroCompraRequestDTO;
import com.heladeria.api.dto.compras.RegistroDetalleCompraDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.EstadoCompra;
import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import com.heladeria.api.util.MonedaPyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraService.class);

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final ProductoProveedorRepository productoProveedorRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AuditoriaService auditoriaService;
    private final ProductoService productoService;

    public CompraService(CompraRepository compraRepository,
                         DetalleCompraRepository detalleCompraRepository,
                         ProveedorRepository proveedorRepository,
                         ProductoRepository productoRepository,
                         ProductoProveedorRepository productoProveedorRepository,
                         MovimientoStockRepository movimientoStockRepository,
                         AuditoriaService auditoriaService,
                         ProductoService productoService) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.productoProveedorRepository = productoProveedorRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.auditoriaService = auditoriaService;
        this.productoService = productoService;
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listarCompras(Long proveedorId, LocalDate fechaDesde, LocalDate fechaHasta, String buscar) {
        List<Compra> compras;
        if (proveedorId != null) {
            compras = compraRepository.findByProveedorIdOrderByFechaEmisionDescIdDesc(proveedorId);
        } else {
            compras = compraRepository.findAllByOrderByFechaEmisionDescIdDesc();
        }

        if (fechaDesde != null) {
            compras = compras.stream()
                    .filter(c -> c.getFechaEmision() != null && !c.getFechaEmision().isBefore(fechaDesde))
                    .collect(Collectors.toList());
        }

        if (fechaHasta != null) {
            compras = compras.stream()
                    .filter(c -> c.getFechaEmision() != null && !c.getFechaEmision().isAfter(fechaHasta))
                    .collect(Collectors.toList());
        }

        if (buscar != null && !buscar.trim().isEmpty()) {
            String b = buscar.trim().toLowerCase();
            compras = compras.stream()
                    .filter(c -> (c.getNumeroFactura() != null && c.getNumeroFactura().toLowerCase().contains(b)) ||
                                 (c.getTimbrado() != null && c.getTimbrado().toLowerCase().contains(b)) ||
                                 (c.getProveedor() != null && c.getProveedor().getRazonSocial() != null && c.getProveedor().getRazonSocial().toLowerCase().contains(b)) ||
                                 (c.getProveedor() != null && c.getProveedor().getRuc() != null && c.getProveedor().getRuc().toLowerCase().contains(b)))
                    .collect(Collectors.toList());
        }

        return compras.stream().map(CompraDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompraDTO obtenerPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra con id " + id + " no encontrada"));
        return new CompraDTO(compra);
    }

    @Transactional
    public CompraDTO registrarCompra(RegistroCompraRequestDTO dto, String usuario) {
        if (dto.getProveedorId() == null) {
            throw new ReglaDeNegocioException("El proveedor es obligatorio.");
        }
        if (dto.getNumeroFactura() == null || dto.getNumeroFactura().trim().isEmpty()) {
            throw new ReglaDeNegocioException("El número de factura es obligatorio.");
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ReglaDeNegocioException("Debe agregar al menos un artículo a la factura de compra.");
        }

        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con id " + dto.getProveedorId() + " no encontrado"));

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setNumeroFactura(dto.getNumeroFactura().trim());
        compra.setTimbrado(dto.getTimbrado() != null ? dto.getTimbrado().trim() : null);
        compra.setFechaEmision(dto.getFechaEmision() != null ? dto.getFechaEmision() : LocalDate.now());
        compra.setFechaVencimiento(dto.getFechaVencimiento());
        compra.setCondicionCompra(dto.getCondicionCompra());
        compra.setObservacion(dto.getObservacion() != null ? dto.getObservacion().trim() : null);
        compra.setUsuario(usuario != null ? usuario : "Sistema");
        compra.setEstado(EstadoCompra.REGISTRADA);

        BigDecimal totalExenta = BigDecimal.ZERO;
        BigDecimal totalIva5 = BigDecimal.ZERO;
        BigDecimal totalIva10 = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (RegistroDetalleCompraDTO itemDTO : dto.getDetalles()) {
            if (itemDTO.getProductoId() == null) {
                throw new ReglaDeNegocioException("El id del artículo es obligatorio.");
            }
            if (itemDTO.getCantidad() == null || itemDTO.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ReglaDeNegocioException("La cantidad debe ser mayor a cero.");
            }
            if (itemDTO.getCostoUnitario() == null || itemDTO.getCostoUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new ReglaDeNegocioException("El costo unitario no puede ser negativo.");
            }

            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto con id " + itemDTO.getProductoId() + " no encontrado"));

            BigDecimal costoUnitario = MonedaPyUtils.redondearGs(itemDTO.getCostoUnitario());
            BigDecimal cantidad = itemDTO.getCantidad();
            BigDecimal subtotal = MonedaPyUtils.redondearGs(costoUnitario.multiply(cantidad));

            TipoIva tipoIva = itemDTO.getTipoIva() != null ? itemDTO.getTipoIva() : producto.getTipoIva();
            if (tipoIva == null) {
                tipoIva = TipoIva.IVA_10;
            }

            BigDecimal montoIva = BigDecimal.ZERO;
            if (tipoIva == TipoIva.IVA_10) {
                montoIva = MonedaPyUtils.calcularIva10(subtotal);
                totalIva10 = totalIva10.add(montoIva);
            } else if (tipoIva == TipoIva.IVA_5) {
                montoIva = MonedaPyUtils.calcularIva5(subtotal);
                totalIva5 = totalIva5.add(montoIva);
            } else {
                totalExenta = totalExenta.add(subtotal);
            }

            total = total.add(subtotal);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setCostoUnitario(costoUnitario);
            detalle.setSubtotal(subtotal);
            detalle.setTipoIva(tipoIva);
            detalle.setMontoIva(montoIva);
            compra.agregarDetalle(detalle);

            // --- 1. IMPACTO EN EL STOCK DEL ARTÍCULO ---
            double stockAnterior = producto.getStock() != null ? producto.getStock().doubleValue() : 0.0;
            double cantDouble = cantidad.doubleValue();
            double stockPosterior = stockAnterior + cantDouble;
            producto.setStock(BigDecimal.valueOf(stockPosterior).setScale(2, RoundingMode.HALF_UP));

            // Actualizar último costo de adquisición si es positivo
            if (costoUnitario.compareTo(BigDecimal.ZERO) > 0) {
                producto.setPrecioCosto(costoUnitario);
            }
            producto = productoRepository.save(producto);
            productoService.sincronizarConCatalogoOperativo(producto);

            // --- 2. VINCULACIÓN O ACTUALIZACIÓN DE COSTO CON EL PROVEEDOR ---
            Optional<ProductoProveedor> vinculoOpt = productoProveedorRepository
                    .findByProductoIdAndProveedorId(producto.getId(), proveedor.getId());
            if (vinculoOpt.isPresent()) {
                ProductoProveedor vinculo = vinculoOpt.get();
                vinculo.setCostoCompra(costoUnitario);
                productoProveedorRepository.save(vinculo);
            } else {
                boolean esPrimerProveedor = productoProveedorRepository.findByProductoId(producto.getId()).isEmpty();
                ProductoProveedor nuevoVinculo = new ProductoProveedor(producto, proveedor, null, costoUnitario, esPrimerProveedor);
                productoProveedorRepository.save(nuevoVinculo);
            }

            // --- 3. ASIENTO EN KARDEX / MOVIMIENTOS DE STOCK ---
            MovimientoStock mov = MovimientoStock.builder()
                    .tipoItem(TipoItemStock.PRODUCTO)
                    .itemId(producto.getId())
                    .itemNombre(producto.getNombre())
                    .tipoMovimiento(TipoMovimientoStock.ENTRADA_COMPRA)
                    .cantidad(cantDouble)
                    .stockAnterior(stockAnterior)
                    .stockPosterior(stockPosterior)
                    .costoUnitario(costoUnitario)
                    .motivo("Compra Factura N° " + compra.getNumeroFactura() + " (" + proveedor.getRazonSocial() + ")")
                    .usuario(usuario != null ? usuario : "Sistema")
                    .build();
            movimientoStockRepository.save(mov);
        }

        compra.setTotalExenta(totalExenta);
        compra.setTotalIva5(totalIva5);
        compra.setTotalIva10(totalIva10);
        compra.setTotal(total);

        Compra guardada = compraRepository.save(compra);
        log.info("Factura de compra registrada con éxito ID {} N° {} Proveedor {}",
                guardada.getId(), guardada.getNumeroFactura(), proveedor.getRazonSocial());

        auditoriaService.registrarLog(
                usuario,
                "COMPRAS",
                "CREACION",
                guardada.getId(),
                "Factura N° " + guardada.getNumeroFactura(),
                "Factura de Compra registrada a '" + proveedor.getRazonSocial() +
                "'. Condición: " + guardada.getCondicionCompra() +
                ", Total: Gs. " + guardada.getTotal() +
                " (" + guardada.getDetalles().size() + " ítems ingresados a stock)",
                null
        );

        return new CompraDTO(guardada);
    }

    @Transactional
    public CompraDTO anularCompra(Long id, String motivo, String usuario) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra con id " + id + " no encontrada"));

        if (compra.getEstado() == EstadoCompra.ANULADA) {
            throw new ReglaDeNegocioException("La factura de compra ya se encuentra anulada.");
        }

        compra.setEstado(EstadoCompra.ANULADA);
        String obsActual = compra.getObservacion() != null ? compra.getObservacion() : "";
        String notaAnulacion = "[ANULADA el " + LocalDate.now() + " por " + (usuario != null ? usuario : "Sistema") +
                (motivo != null && !motivo.trim().isEmpty() ? " - Motivo: " + motivo.trim() : "") + "]";
        compra.setObservacion((obsActual + " " + notaAnulacion).trim());

        // Reversión de stock de cada artículo ingresado
        for (DetalleCompra detalle : compra.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto != null) {
                double stockAnterior = producto.getStock() != null ? producto.getStock().doubleValue() : 0.0;
                double cantDouble = detalle.getCantidad() != null ? detalle.getCantidad().doubleValue() : 0.0;
                double stockPosterior = Math.max(0.0, stockAnterior - cantDouble);
                producto.setStock(BigDecimal.valueOf(stockPosterior).setScale(2, RoundingMode.HALF_UP));
                producto = productoRepository.save(producto);
                productoService.sincronizarConCatalogoOperativo(producto);

                MovimientoStock movReversion = MovimientoStock.builder()
                        .tipoItem(TipoItemStock.PRODUCTO)
                        .itemId(producto.getId())
                        .itemNombre(producto.getNombre())
                        .tipoMovimiento(TipoMovimientoStock.REINTEGRO_CANCELACION)
                        .cantidad(cantDouble)
                        .stockAnterior(stockAnterior)
                        .stockPosterior(stockPosterior)
                        .costoUnitario(detalle.getCostoUnitario())
                        .motivo("Anulación Factura Compra N° " + compra.getNumeroFactura() +
                                (motivo != null && !motivo.trim().isEmpty() ? " - " + motivo.trim() : ""))
                        .usuario(usuario != null ? usuario : "Sistema")
                        .build();
                movimientoStockRepository.save(movReversion);
            }
        }

        Compra actualizada = compraRepository.save(compra);
        log.warn("Factura de compra anulada ID {} N° {}. Stock revertido.", actualizada.getId(), actualizada.getNumeroFactura());

        auditoriaService.registrarLog(
                usuario,
                "COMPRAS",
                "ANULACION",
                actualizada.getId(),
                "Factura N° " + actualizada.getNumeroFactura(),
                "Factura de Compra ANULADA. Total: Gs. " + actualizada.getTotal() +
                ". Motivo: " + (motivo != null && !motivo.isBlank() ? motivo.trim() : "Sin motivo especificado") +
                ". Se ejecutó reversión automática de stock.",
                null
        );

        return new CompraDTO(actualizada);
    }
}
