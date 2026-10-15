package com.heladeria.api.services;

import com.heladeria.api.dto.inventario.*;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.TipoItemStock;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.heladeria.api.entities.enums.EstadoTomaInventario;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioService {

    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoSimpleRepository productoSimpleRepository;
    private final SaborRepository saborRepository;
    private final PresentacionRepository presentacionRepository;
    private final TomaInventarioRepository tomaInventarioRepository;
    private final DetalleTomaInventarioRepository detalleTomaInventarioRepository;

    public InventarioService(MovimientoStockRepository movimientoStockRepository,
                             ProductoSimpleRepository productoSimpleRepository,
                             SaborRepository saborRepository,
                             PresentacionRepository presentacionRepository,
                             TomaInventarioRepository tomaInventarioRepository,
                             DetalleTomaInventarioRepository detalleTomaInventarioRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.productoSimpleRepository = productoSimpleRepository;
        this.saborRepository = saborRepository;
        this.presentacionRepository = presentacionRepository;
        this.tomaInventarioRepository = tomaInventarioRepository;
        this.detalleTomaInventarioRepository = detalleTomaInventarioRepository;
    }

    @Transactional
    public MovimientoStockDTO registrarMovimientoManual(RegistrarMovimientoDTO dto, String usuario) {
        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new ReglaDeNegocioException("La cantidad debe ser estrictamente mayor a cero.");
        }

        String itemNombre = "";
        double stockAnterior = 0.0;
        double stockPosterior = 0.0;
        String unidad = "UNIDADES";

        if (dto.getTipoItem() == TipoItemStock.PRODUCTO) {
            ProductoSimple prod = productoSimpleRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto simple con id " + dto.getItemId() + " no encontrado"));
            itemNombre = prod.getNombre();
            stockAnterior = prod.getStock() != null ? prod.getStock().doubleValue() : 0.0;
            unidad = "UNIDADES";

            stockPosterior = calcularNuevoStock(stockAnterior, dto.getCantidad(), dto.getTipoMovimiento());
            prod.setStock((int) Math.round(stockPosterior));
            productoSimpleRepository.save(prod);

        } else if (dto.getTipoItem() == TipoItemStock.SABOR) {
            Sabor sabor = saborRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sabor con id " + dto.getItemId() + " no encontrado"));
            itemNombre = sabor.getNombre();
            stockAnterior = sabor.getStockKilos() != null ? sabor.getStockKilos() : 0.0;
            unidad = "KG";

            stockPosterior = Math.round(calcularNuevoStock(stockAnterior, dto.getCantidad(), dto.getTipoMovimiento()) * 1000.0) / 1000.0;
            sabor.setStockKilos(stockPosterior);
            sabor.setDisponible(stockPosterior > 0.001);
            saborRepository.save(sabor);

        } else if (dto.getTipoItem() == TipoItemStock.ENVASE) {
            Presentacion pres = presentacionRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Presentación/Envase con id " + dto.getItemId() + " no encontrado"));
            itemNombre = pres.getNombre();
            stockAnterior = pres.getStock() != null ? pres.getStock().doubleValue() : 0.0;
            unidad = "UNIDADES";

            stockPosterior = calcularNuevoStock(stockAnterior, dto.getCantidad(), dto.getTipoMovimiento());
            pres.setStock((int) Math.round(stockPosterior));
            presentacionRepository.save(pres);
        }

        MovimientoStock movimiento = MovimientoStock.builder()
                .fecha(LocalDateTime.now())
                .tipoItem(dto.getTipoItem())
                .itemId(dto.getItemId())
                .itemNombre(itemNombre)
                .tipoMovimiento(dto.getTipoMovimiento())
                .cantidad(dto.getCantidad())
                .stockAnterior(stockAnterior)
                .stockPosterior(stockPosterior)
                .motivo(dto.getMotivo() != null && !dto.getMotivo().isBlank() ? dto.getMotivo().trim() : "Movimiento manual de inventario")
                .usuario(usuario != null ? usuario : "SISTEMA")
                .costoUnitario(dto.getCostoUnitario())
                .build();

        movimiento = movimientoStockRepository.save(movimiento);
        log.info("Movimiento registrado: [{}] {} - Cant: {} (Ant: {}, Post: {}) por {}",
                dto.getTipoMovimiento(), itemNombre, dto.getCantidad(), stockAnterior, stockPosterior, usuario);

        return toDTO(movimiento, unidad);
    }

    @Transactional
    public void registrarMovimientoInterno(TipoItemStock tipoItem, Long itemId, String itemNombre,
                                           TipoMovimientoStock tipoMovimiento, double cantidad,
                                           double stockAnterior, double stockPosterior,
                                           String motivo, String usuario) {
        try {
            MovimientoStock mov = MovimientoStock.builder()
                    .fecha(LocalDateTime.now())
                    .tipoItem(tipoItem)
                    .itemId(itemId)
                    .itemNombre(itemNombre)
                    .tipoMovimiento(tipoMovimiento)
                    .cantidad(cantidad)
                    .stockAnterior(stockAnterior)
                    .stockPosterior(stockPosterior)
                    .motivo(motivo)
                    .usuario(usuario != null ? usuario : "POS")
                    .build();
            movimientoStockRepository.save(mov);
        } catch (Exception e) {
            log.error("Error al registrar movimiento automático de Kardex: ", e);
        }
    }

    private double calcularNuevoStock(double actual, double cantidad, TipoMovimientoStock tipo) {
        return switch (tipo) {
            case ENTRADA_COMPRA, ENTRADA_PRODUCCION, REINTEGRO_CANCELACION -> actual + cantidad;
            case SALIDA_VENTA, SALIDA_MERMA -> Math.max(0.0, actual - cantidad);
            case AJUSTE_INVENTARIO -> cantidad;
        };
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerKardex(LocalDate inicio, LocalDate fin, TipoItemStock tipoItem, TipoMovimientoStock tipoMovimiento, Long itemId) {
        LocalDateTime dtInicio = inicio != null ? inicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime dtFin = fin != null ? fin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        List<MovimientoStock> movimientos = movimientoStockRepository.filtrarMovimientos(dtInicio, dtFin, tipoItem, tipoMovimiento, itemId);
        List<MovimientoStockDTO> dtos = new ArrayList<>();
        for (MovimientoStock m : movimientos) {
            String u = m.getTipoItem() == TipoItemStock.SABOR ? "KG" : "UNIDADES";
            dtos.add(toDTO(m, u));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public ReporteValorizacionDTO obtenerValorizacion() {
        ReporteValorizacionDTO reporte = new ReporteValorizacionDTO();
        List<ValorizacionItemDTO> items = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;
        int agotados = 0;
        int bajoMin = 0;

        // 1. Productos Simples
        List<ProductoSimple> productos = productoSimpleRepository.findAll();
        for (ProductoSimple p : productos) {
            double stock = p.getStock() != null ? p.getStock().doubleValue() : 0.0;
            double stockMin = p.getStockMinimo() != null ? p.getStockMinimo().doubleValue() : 5.0;
            BigDecimal precio = p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO;
            BigDecimal subtotal = MonedaPyUtils.redondearGs(precio.multiply(BigDecimal.valueOf(stock)));
            totalGeneral = totalGeneral.add(subtotal);

            String estado = stock <= 0 ? "AGOTADO" : (stock <= stockMin ? "BAJO" : "OPTIMO");
            if ("AGOTADO".equals(estado)) agotados++;
            else if ("BAJO".equals(estado)) bajoMin++;

            items.add(new ValorizacionItemDTO(p.getId(), p.getNombre(), TipoItemStock.PRODUCTO, stock, stockMin, "UNIDADES", precio, subtotal, estado));
        }

        // 2. Presentaciones / Envases
        List<Presentacion> presentaciones = presentacionRepository.findAll();
        for (Presentacion pres : presentaciones) {
            double stock = pres.getStock() != null ? pres.getStock().doubleValue() : 0.0;
            double stockMin = pres.getStockMinimo() != null ? pres.getStockMinimo().doubleValue() : 10.0;
            BigDecimal precio = pres.getPrecio() != null ? pres.getPrecio() : BigDecimal.ZERO;
            BigDecimal subtotal = MonedaPyUtils.redondearGs(precio.multiply(BigDecimal.valueOf(stock)));
            totalGeneral = totalGeneral.add(subtotal);

            String estado = stock <= 0 ? "AGOTADO" : (stock <= stockMin ? "BAJO" : "OPTIMO");
            if ("AGOTADO".equals(estado)) agotados++;
            else if ("BAJO".equals(estado)) bajoMin++;

            items.add(new ValorizacionItemDTO(pres.getId(), pres.getNombre(), TipoItemStock.ENVASE, stock, stockMin, "UNIDADES", precio, subtotal, estado));
        }

        // 3. Sabores de Helado (Valorizados según precio promedio estimado por kilo: 60.000 Gs o según pote 1kg)
        BigDecimal precioEstimadoKiloHelado = new BigDecimal("60000");
        List<Sabor> sabores = saborRepository.findAll();
        for (Sabor s : sabores) {
            double stockKilos = s.getStockKilos() != null ? s.getStockKilos() : 0.0;
            double stockMin = s.getStockMinimoKilos() != null ? s.getStockMinimoKilos() : 2.0;
            BigDecimal subtotal = MonedaPyUtils.redondearGs(precioEstimadoKiloHelado.multiply(BigDecimal.valueOf(stockKilos)));
            totalGeneral = totalGeneral.add(subtotal);

            String estado = (stockKilos <= 0.001 || !Boolean.TRUE.equals(s.getDisponible())) ? "AGOTADO" : (stockKilos <= stockMin ? "BAJO" : "OPTIMO");
            if ("AGOTADO".equals(estado)) agotados++;
            else if ("BAJO".equals(estado)) bajoMin++;

            items.add(new ValorizacionItemDTO(s.getId(), s.getNombre(), TipoItemStock.SABOR, stockKilos, stockMin, "KG", precioEstimadoKiloHelado, subtotal, estado));
        }

        reporte.setItems(items);
        reporte.setTotalArticulos(items.size());
        reporte.setTotalAgotados(agotados);
        reporte.setTotalBajoMinimo(bajoMin);
        reporte.setTotalValorizadoGeneral(totalGeneral);
        return reporte;
    }

    @Transactional(readOnly = true)
    public KpisInventarioDTO obtenerKpis(LocalDate inicio, LocalDate fin) {
        LocalDateTime dtInicio = inicio != null ? inicio.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime dtFin = fin != null ? fin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        List<MovimientoStock> movimientos = movimientoStockRepository.findByFechaBetweenOrderByFechaDesc(dtInicio, dtFin);

        double totalEntradas = 0.0;
        double totalSalidasVenta = 0.0;
        double totalMermas = 0.0;
        double totalAjustes = 0.0;

        for (MovimientoStock m : movimientos) {
            switch (m.getTipoMovimiento()) {
                case ENTRADA_COMPRA, ENTRADA_PRODUCCION -> totalEntradas += m.getCantidad();
                case SALIDA_VENTA -> totalSalidasVenta += m.getCantidad();
                case SALIDA_MERMA -> totalMermas += m.getCantidad();
                case AJUSTE_INVENTARIO -> totalAjustes += m.getCantidad();
                default -> {}
            }
        }

        ReporteValorizacionDTO val = obtenerValorizacion();

        return new KpisInventarioDTO(
                Math.round(totalEntradas * 100.0) / 100.0,
                Math.round(totalSalidasVenta * 100.0) / 100.0,
                Math.round(totalMermas * 100.0) / 100.0,
                Math.round(totalAjustes * 100.0) / 100.0,
                movimientos.size(),
                val.getTotalValorizadoGeneral()
        );
    }

    @Transactional(readOnly = true)
    public List<ArticuloInventarioDTO> listarArticulosDisponibles() {
        List<ArticuloInventarioDTO> lista = new ArrayList<>();

        for (ProductoSimple p : productoSimpleRepository.findAll()) {
            lista.add(new ArticuloInventarioDTO(p.getId(), p.getNombre(), TipoItemStock.PRODUCTO,
                    p.getStock() != null ? p.getStock().doubleValue() : 0.0, "UNIDADES", p.getPrecio()));
        }

        for (Presentacion pres : presentacionRepository.findAll()) {
            lista.add(new ArticuloInventarioDTO(pres.getId(), pres.getNombre(), TipoItemStock.ENVASE,
                    pres.getStock() != null ? pres.getStock().doubleValue() : 0.0, "UNIDADES", pres.getPrecio()));
        }

        for (Sabor s : saborRepository.findAll()) {
            lista.add(new ArticuloInventarioDTO(s.getId(), s.getNombre(), TipoItemStock.SABOR,
                    s.getStockKilos() != null ? s.getStockKilos() : 0.0, "KG", new BigDecimal("60000")));
        }

        return lista;
    }

    @Transactional
    public TomaInventarioDTO crearYAplicarTomaInventario(CrearTomaInventarioDTO dto, String usuarioAuditor) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ReglaDeNegocioException("La toma de inventario debe incluir al menos un artículo contado.");
        }

        String auditor = (usuarioAuditor != null && !usuarioAuditor.isBlank()) ? usuarioAuditor : "AUDITOR";
        TomaInventario toma = new TomaInventario();
        toma.setFecha(LocalDateTime.now());
        toma.setUsuarioAuditor(auditor);
        toma.setObservaciones(dto.getObservaciones() != null ? dto.getObservaciones().trim() : "Auditoría física de inventario");
        toma.setEstado(EstadoTomaInventario.APLICADA);

        int totalArticulos = 0;
        int totalDiferencias = 0;
        BigDecimal impactoNeto = BigDecimal.ZERO;

        toma = tomaInventarioRepository.save(toma);

        List<DetalleTomaInventario> detallesPersistidos = new ArrayList<>();

        for (ItemConteoDTO item : dto.getDetalles()) {
            if (item.getItemId() == null || item.getTipoItem() == null || item.getStockFisico() == null) {
                continue;
            }

            totalArticulos++;
            double stockSistema = 0.0;
            String itemNombre = item.getItemNombre() != null ? item.getItemNombre() : "";
            String unidad = "UNIDADES";
            BigDecimal costoUnitario = item.getCostoUnitario() != null ? item.getCostoUnitario() : BigDecimal.ZERO;

            if (item.getTipoItem() == TipoItemStock.PRODUCTO) {
                var optProd = productoSimpleRepository.findById(item.getItemId());
                if (optProd.isPresent()) {
                    var prod = optProd.get();
                    stockSistema = prod.getStock() != null ? prod.getStock().doubleValue() : 0.0;
                    if (itemNombre.isBlank()) itemNombre = prod.getNombre();
                    if (costoUnitario.compareTo(BigDecimal.ZERO) == 0 && prod.getPrecio() != null) {
                        costoUnitario = prod.getPrecio();
                    }
                    double dif = item.getStockFisico() - stockSistema;
                    if (Math.abs(dif) > 0.0001) {
                        totalDiferencias++;
                        prod.setStock((int) Math.round(item.getStockFisico()));
                        productoSimpleRepository.save(prod);

                        registrarMovimientoInterno(
                                TipoItemStock.PRODUCTO,
                                prod.getId(),
                                prod.getNombre(),
                                TipoMovimientoStock.AJUSTE_INVENTARIO,
                                item.getStockFisico(),
                                stockSistema,
                                item.getStockFisico(),
                                "Auditoría Física #" + toma.getId() + (toma.getObservaciones() != null ? " - " + toma.getObservaciones() : ""),
                                auditor
                        );
                    }
                }
            } else if (item.getTipoItem() == TipoItemStock.SABOR) {
                unidad = "KG";
                var optSabor = saborRepository.findById(item.getItemId());
                if (optSabor.isPresent()) {
                    var sabor = optSabor.get();
                    stockSistema = sabor.getStockKilos() != null ? sabor.getStockKilos() : 0.0;
                    if (itemNombre.isBlank()) itemNombre = sabor.getNombre();
                    if (costoUnitario.compareTo(BigDecimal.ZERO) == 0) {
                        costoUnitario = new BigDecimal("45000");
                    }
                    double dif = Math.round((item.getStockFisico() - stockSistema) * 1000.0) / 1000.0;
                    if (Math.abs(dif) > 0.001) {
                        totalDiferencias++;
                        double nuevoStock = Math.max(0.0, Math.round(item.getStockFisico() * 1000.0) / 1000.0);
                        sabor.setStockKilos(nuevoStock);
                        sabor.setDisponible(nuevoStock > 0.001);
                        saborRepository.save(sabor);

                        registrarMovimientoInterno(
                                TipoItemStock.SABOR,
                                sabor.getId(),
                                sabor.getNombre(),
                                TipoMovimientoStock.AJUSTE_INVENTARIO,
                                nuevoStock,
                                stockSistema,
                                nuevoStock,
                                "Auditoría Física #" + toma.getId() + (toma.getObservaciones() != null ? " - " + toma.getObservaciones() : ""),
                                auditor
                        );
                    }
                }
            } else if (item.getTipoItem() == TipoItemStock.ENVASE) {
                var optPres = presentacionRepository.findById(item.getItemId());
                if (optPres.isPresent()) {
                    var pres = optPres.get();
                    stockSistema = pres.getStock() != null ? pres.getStock().doubleValue() : 0.0;
                    if (itemNombre.isBlank()) itemNombre = pres.getNombre();
                    if (costoUnitario.compareTo(BigDecimal.ZERO) == 0 && pres.getPrecio() != null) {
                        costoUnitario = pres.getPrecio().multiply(new BigDecimal("0.35"));
                    }
                    double dif = item.getStockFisico() - stockSistema;
                    if (Math.abs(dif) > 0.0001) {
                        totalDiferencias++;
                        pres.setStock((int) Math.round(item.getStockFisico()));
                        presentacionRepository.save(pres);

                        registrarMovimientoInterno(
                                TipoItemStock.ENVASE,
                                pres.getId(),
                                pres.getNombre(),
                                TipoMovimientoStock.AJUSTE_INVENTARIO,
                                item.getStockFisico(),
                                stockSistema,
                                item.getStockFisico(),
                                "Auditoría Física #" + toma.getId() + (toma.getObservaciones() != null ? " - " + toma.getObservaciones() : ""),
                                auditor
                        );
                    }
                }
            }

            double dif = item.getTipoItem() == TipoItemStock.SABOR
                    ? (Math.round((item.getStockFisico() - stockSistema) * 1000.0) / 1000.0)
                    : (item.getStockFisico() - stockSistema);

            costoUnitario = MonedaPyUtils.redondearGs(costoUnitario);
            BigDecimal difValorizada = MonedaPyUtils.redondearGs(costoUnitario.multiply(BigDecimal.valueOf(dif)));
            impactoNeto = impactoNeto.add(difValorizada);

            DetalleTomaInventario det = new DetalleTomaInventario();
            det.setTomaInventario(toma);
            det.setTipoItem(item.getTipoItem());
            det.setItemId(item.getItemId());
            det.setItemNombre(itemNombre);
            det.setUnidad(unidad);
            det.setStockSistema(stockSistema);
            det.setStockFisico(item.getStockFisico());
            det.setDiferencia(dif);
            det.setCostoUnitario(costoUnitario);
            det.setDiferenciaValorizada(difValorizada);

            detallesPersistidos.add(detalleTomaInventarioRepository.save(det));
        }

        toma.setTotalArticulos(totalArticulos);
        toma.setTotalDiferencias(totalDiferencias);
        toma.setImpactoNeto(MonedaPyUtils.redondearGs(impactoNeto));
        toma.setDetalles(detallesPersistidos);
        toma = tomaInventarioRepository.save(toma);

        log.info("Auditoría Física #{} aplicada: {} artículos contados, {} discrepancias, impacto neto: Gs. {}",
                toma.getId(), totalArticulos, totalDiferencias, impactoNeto);

        return toTomaDTO(toma);
    }

    @Transactional(readOnly = true)
    public List<TomaInventarioDTO> listarTomasInventario() {
        return tomaInventarioRepository.findAllOrderByFechaDesc().stream()
                .map(this::toTomaResumenDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TomaInventarioDTO obtenerTomaInventario(Long id) {
        TomaInventario toma = tomaInventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Toma de inventario #" + id + " no encontrada"));
        return toTomaDTO(toma);
    }

    private TomaInventarioDTO toTomaResumenDTO(TomaInventario t) {
        return new TomaInventarioDTO(
                t.getId(),
                t.getFecha(),
                t.getUsuarioAuditor(),
                t.getObservaciones(),
                t.getEstado(),
                t.getTotalArticulos(),
                t.getTotalDiferencias(),
                t.getImpactoNeto(),
                new ArrayList<>()
        );
    }

    private TomaInventarioDTO toTomaDTO(TomaInventario t) {
        List<DetalleTomaInventarioDTO> detDTOs = (t.getDetalles() != null && !t.getDetalles().isEmpty()
                ? t.getDetalles()
                : detalleTomaInventarioRepository.findByTomaInventarioIdOrderByIdAsc(t.getId()))
                .stream()
                .map(d -> new DetalleTomaInventarioDTO(
                        d.getId(),
                        d.getTipoItem(),
                        d.getItemId(),
                        d.getItemNombre(),
                        d.getUnidad(),
                        d.getStockSistema(),
                        d.getStockFisico(),
                        d.getDiferencia(),
                        d.getCostoUnitario(),
                        d.getDiferenciaValorizada()
                ))
                .toList();

        return new TomaInventarioDTO(
                t.getId(),
                t.getFecha(),
                t.getUsuarioAuditor(),
                t.getObservaciones(),
                t.getEstado(),
                t.getTotalArticulos(),
                t.getTotalDiferencias(),
                t.getImpactoNeto(),
                detDTOs
        );
    }

    private MovimientoStockDTO toDTO(MovimientoStock m, String unidad) {
        return new MovimientoStockDTO(
                m.getId(),
                m.getFecha(),
                m.getTipoItem(),
                m.getItemId(),
                m.getItemNombre(),
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getStockAnterior(),
                m.getStockPosterior(),
                m.getMotivo(),
                m.getUsuario(),
                m.getCostoUnitario(),
                unidad
        );
    }
}
