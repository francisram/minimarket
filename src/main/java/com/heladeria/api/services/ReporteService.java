package com.heladeria.api.services;

import com.heladeria.api.dto.reportes.*;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoEntrega;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.util.MonedaPyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.heladeria.api.repositories.NotaCreditoRepository notaCreditoRepository;
    private final SesionCajaRepository sesionCajaRepository;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReporteService(PedidoRepository pedidoRepository,
                          SesionCajaRepository sesionCajaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.sesionCajaRepository = sesionCajaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardReporteDTO obtenerDashboard(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepository.findByFechaCreacionBetweenAndEstadoNotOrderByFechaCreacionDesc(inicio, fin, EstadoPedido.CANCELADO);

        DashboardReporteDTO dto = new DashboardReporteDTO();
        dto.setPeriodoInicio(inicio);
        dto.setPeriodoFin(fin);

        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjetaDebito = BigDecimal.ZERO;
        BigDecimal totalTarjetaCredito = BigDecimal.ZERO;
        BigDecimal totalTransferenciaQr = BigDecimal.ZERO;
        BigDecimal totalVentasCredito = BigDecimal.ZERO;

        long totalFacturas = 0;
        long totalTickets = 0;

        BigDecimal totalExentas = BigDecimal.ZERO;
        BigDecimal totalGravada5 = BigDecimal.ZERO;
        BigDecimal totalIva5 = BigDecimal.ZERO;
        BigDecimal totalGravada10 = BigDecimal.ZERO;
        BigDecimal totalIva10 = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        Map<LocalDate, VentaDiariaDTO> ventasPorDiaMap = new TreeMap<>();
        Map<String, ItemRankingDTO> productosMap = new HashMap<>();
        Map<Long, SaborRankingDTO> saboresMap = new HashMap<>();

        for (Pedido p : pedidos) {
            BigDecimal totalPedido = MonedaPyUtils.redondearGs(p.getTotal());
            totalVentas = totalVentas.add(totalPedido);

            // Método de pago (una venta a crédito todavía no se cobró: no entra en ningún método)
            if (p.getCondicionVenta() == CondicionVenta.CREDITO) {
                totalVentasCredito = totalVentasCredito.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.EFECTIVO) {
                totalEfectivo = totalEfectivo.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TARJETA_DEBITO) {
                totalTarjetaDebito = totalTarjetaDebito.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TARJETA_CREDITO) {
                totalTarjetaCredito = totalTarjetaCredito.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TRANSFERENCIA_QR) {
                totalTransferenciaQr = totalTransferenciaQr.add(totalPedido);
            }

            // Tipo comprobante
            if (p.getTipoComprobante() == TipoComprobante.FACTURA) {
                totalFacturas++;
            } else {
                totalTickets++;
            }

            // Fiscal
            totalExentas = totalExentas.add(p.getTotalExentas() != null ? p.getTotalExentas() : BigDecimal.ZERO);
            totalGravada5 = totalGravada5.add(p.getTotalGravada5() != null ? p.getTotalGravada5() : BigDecimal.ZERO);
            totalIva5 = totalIva5.add(p.getTotalIva5() != null ? p.getTotalIva5() : BigDecimal.ZERO);
            totalGravada10 = totalGravada10.add(p.getTotalGravada10() != null ? p.getTotalGravada10() : BigDecimal.ZERO);
            totalIva10 = totalIva10.add(p.getTotalIva10() != null ? p.getTotalIva10() : BigDecimal.ZERO);
            totalIva = totalIva.add(p.getTotalIva() != null ? p.getTotalIva() : BigDecimal.ZERO);

            // Serie diaria
            LocalDate fechaPedido = p.getFechaCreacion().toLocalDate();
            VentaDiariaDTO vd = ventasPorDiaMap.computeIfAbsent(fechaPedido, f -> new VentaDiariaDTO(f, BigDecimal.ZERO, 0));
            vd.setTotal(vd.getTotal().add(totalPedido));
            vd.setCantidadPedidos(vd.getCantidadPedidos() + 1);

            // Detalles: productos y sabores
            if (p.getDetalles() != null) {
                for (DetallePedido d : p.getDetalles()) {
                    if (d.getTipoItem() == TipoItemPedido.HELADO && d.getPresentacion() != null) {
                        String clave = "PRES_" + d.getPresentacion().getId();
                        ItemRankingDTO ir = productosMap.computeIfAbsent(clave, k ->
                                new ItemRankingDTO(d.getPresentacion().getId(), d.getPresentacion().getNombre(), "HELADO", 0, BigDecimal.ZERO));
                        ir.setCantidad(ir.getCantidad() + d.getCantidad());
                        ir.setTotalRecaudado(ir.getTotalRecaudado().add(d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO));

                        // Sabores
                        if (d.getSabores() != null && !d.getSabores().isEmpty()) {
                            double kilosPorSabor = 0.0;
                            if (d.getPresentacion().getPesoGramosAprox() != null && d.getPresentacion().getPesoGramosAprox() > 0) {
                                kilosPorSabor = (((double) d.getPresentacion().getPesoGramosAprox() * d.getCantidad())
                                        / (1000.0 * d.getSabores().size()));
                            }
                            for (Sabor s : d.getSabores()) {
                                SaborRankingDTO sr = saboresMap.computeIfAbsent(s.getId(), k ->
                                        new SaborRankingDTO(s.getId(), s.getNombre(), s.getCategoria() != null ? s.getCategoria().name() : null, 0, 0.0));
                                sr.setCantidadElecciones(sr.getCantidadElecciones() + d.getCantidad());
                                sr.setKilosEstimados(Math.round((sr.getKilosEstimados() + kilosPorSabor) * 100.0) / 100.0);
                            }
                        }
                    } else if (d.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && d.getProductoSimple() != null) {
                        String clave = "PROD_" + d.getProductoSimple().getId();
                        ItemRankingDTO ir = productosMap.computeIfAbsent(clave, k ->
                                new ItemRankingDTO(d.getProductoSimple().getId(), d.getProductoSimple().getNombre(), "PRODUCTO_SIMPLE", 0, BigDecimal.ZERO));
                        ir.setCantidad(ir.getCantidad() + d.getCantidad());
                        ir.setTotalRecaudado(ir.getTotalRecaudado().add(d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO));
                    } else if (d.getTipoItem() == TipoItemPedido.TOPPING && d.getToppings() != null && !d.getToppings().isEmpty()) {
                        String clave = "TOP_" + d.getToppings().get(0).getId();
                        ItemRankingDTO ir = productosMap.computeIfAbsent(clave, k ->
                                new ItemRankingDTO(d.getToppings().get(0).getId(), d.getToppings().get(0).getNombre(), "TOPPING", 0, BigDecimal.ZERO));
                        ir.setCantidad(ir.getCantidad() + d.getCantidad());
                        ir.setTotalRecaudado(ir.getTotalRecaudado().add(d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO));
                    }
                }
            }
        }

        // Notas de crédito del período: restan de las ventas, de los métodos de pago, del detalle fiscal y de la serie diaria
        BigDecimal totalNotasCredito = BigDecimal.ZERO;
        for (NotaCredito nc : notaCreditoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)) {
            BigDecimal t = nc.getTotal();
            totalNotasCredito = totalNotasCredito.add(t);
            totalVentas = totalVentas.subtract(t);
            Pedido f = nc.getPedido();
            if (f.getCondicionVenta() == CondicionVenta.CREDITO) {
                totalVentasCredito = totalVentasCredito.subtract(t);
            } else if (f.getMetodoPago() == MetodoPago.EFECTIVO) {
                totalEfectivo = totalEfectivo.subtract(t);
            } else if (f.getMetodoPago() == MetodoPago.TARJETA_DEBITO) {
                totalTarjetaDebito = totalTarjetaDebito.subtract(t);
            } else if (f.getMetodoPago() == MetodoPago.TARJETA_CREDITO) {
                totalTarjetaCredito = totalTarjetaCredito.subtract(t);
            } else if (f.getMetodoPago() == MetodoPago.TRANSFERENCIA_QR) {
                totalTransferenciaQr = totalTransferenciaQr.subtract(t);
            }
            totalExentas = totalExentas.subtract(nc.getTotalExentas());
            totalGravada5 = totalGravada5.subtract(nc.getTotalGravada5());
            totalIva5 = totalIva5.subtract(nc.getTotalIva5());
            totalGravada10 = totalGravada10.subtract(nc.getTotalGravada10());
            totalIva10 = totalIva10.subtract(nc.getTotalIva10());
            totalIva = totalIva.subtract(nc.getTotalIva());
            VentaDiariaDTO vd = ventasPorDiaMap.computeIfAbsent(nc.getFecha().toLocalDate(), d -> new VentaDiariaDTO(d, BigDecimal.ZERO, 0));
            vd.setTotal(vd.getTotal().subtract(t));
        }
        dto.setTotalNotasCredito(totalNotasCredito);
        dto.setTotalVentasCredito(totalVentasCredito);

        dto.setTotalVentas(totalVentas);
        dto.setCantidadVentas(pedidos.size());
        if (!pedidos.isEmpty()) {
            dto.setTicketPromedio(totalVentas.divide(BigDecimal.valueOf(pedidos.size()), 0, RoundingMode.HALF_UP));
        }

        dto.setTotalEfectivo(totalEfectivo);
        dto.setTotalTarjetaDebito(totalTarjetaDebito);
        dto.setTotalTarjetaCredito(totalTarjetaCredito);
        dto.setTotalTransferenciaQr(totalTransferenciaQr);

        dto.setTotalFacturas(totalFacturas);
        dto.setTotalTickets(totalTickets);

        dto.setTotalExentas(totalExentas);
        dto.setTotalGravada5(totalGravada5);
        dto.setTotalIva5(totalIva5);
        dto.setTotalGravada10(totalGravada10);
        dto.setTotalIva10(totalIva10);
        dto.setTotalIva(totalIva);

        dto.setVentasPorDia(new ArrayList<>(ventasPorDiaMap.values()));

        List<ItemRankingDTO> topProductos = productosMap.values().stream()
                .sorted(Comparator.comparingLong(ItemRankingDTO::getCantidad).reversed())
                .limit(5)
                .toList();
        dto.setTopProductos(topProductos);

        List<SaborRankingDTO> topSabores = saboresMap.values().stream()
                .sorted(Comparator.comparingLong(SaborRankingDTO::getCantidadElecciones).reversed())
                .limit(5)
                .toList();
        dto.setTopSabores(topSabores);

        return dto;
    }

    @Transactional(readOnly = true)
    public ReporteVentasDTO obtenerReporteVentas(LocalDate fechaInicio, LocalDate fechaFin,
                                                  MetodoPago metodoPago, TipoComprobante tipoComprobante,
                                                  TipoEntrega tipoEntrega, String cajero) {
        return obtenerReporteVentas(fechaInicio, fechaFin, metodoPago, tipoComprobante, tipoEntrega, cajero, null);
    }

    @Transactional(readOnly = true)
    public ReporteVentasDTO obtenerReporteVentas(LocalDate fechaInicio, LocalDate fechaFin,
                                                  MetodoPago metodoPago, TipoComprobante tipoComprobante,
                                                  TipoEntrega tipoEntrega, String cajero, String trxId) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin);

        ReporteVentasDTO reporte = new ReporteVentasDTO();
        reporte.setPeriodoInicio(inicio);
        reporte.setPeriodoFin(fin);

        BigDecimal totalVentas = BigDecimal.ZERO;
        long cantidadVentas = 0;
        Map<MetodoPago, BigDecimal> resumenMetodos = new HashMap<>();
        List<PedidoResumenReporteDTO> listaDTO = new ArrayList<>();

        java.util.function.Predicate<Pedido> filtro = p -> {
            if (metodoPago != null && p.getMetodoPago() != metodoPago) {
                return false;
            }
            if (tipoComprobante != null && p.getTipoComprobante() != tipoComprobante) {
                return false;
            }
            if (tipoEntrega != null && p.getTipoEntrega() != tipoEntrega) {
                return false;
            }
            if (cajero != null && !cajero.isBlank()) {
                String cajeroPedido = (p.getSesionCaja() != null && p.getSesionCaja().getUsuarioApertura() != null)
                        ? p.getSesionCaja().getUsuarioApertura().getUsername() : null;
                if (cajeroPedido == null || !cajeroPedido.equalsIgnoreCase(cajero.trim())) {
                    return false;
                }
            }
            if (trxId != null && !trxId.isBlank()) {
                if (p.getTrxId() == null || !p.getTrxId().toLowerCase().contains(trxId.trim().toLowerCase())) {
                    return false;
                }
            }
            return true;
        };

        for (Pedido p : pedidos) {
            if (!filtro.test(p)) {
                continue;
            }

            PedidoResumenReporteDTO item = new PedidoResumenReporteDTO();
            item.setId(p.getId());
            item.setFechaCreacion(p.getFechaCreacion());
            item.setClienteNombre(p.getClienteNombre() != null ? p.getClienteNombre() : "Consumidor Final");
            item.setClienteRuc(p.getClienteRuc());
            item.setTipoComprobante(p.getTipoComprobante());
            item.setCondicionVenta(p.getCondicionVenta());
            item.setNumeroFactura(p.getNumeroFactura());
            item.setNumeroVoucher(p.getNumeroVoucher());
            item.setTrxId(p.getTrxId());
            item.setMetodoPago(p.getMetodoPago());
            item.setTipoEntrega(p.getTipoEntrega());
            item.setTotal(MonedaPyUtils.redondearGs(p.getTotal()));
            item.setEstado(p.getEstado());
            if (p.getSesionCaja() != null && p.getSesionCaja().getUsuarioApertura() != null) {
                item.setCajero(p.getSesionCaja().getUsuarioApertura().getUsername());
            }

            listaDTO.add(item);

            if (p.getEstado() != EstadoPedido.CANCELADO) {
                BigDecimal tot = item.getTotal();
                totalVentas = totalVentas.add(tot);
                cantidadVentas++;
                resumenMetodos.merge(p.getMetodoPago(), tot, BigDecimal::add);
            }
        }

        // Notas de crédito del período (de las facturas que pasan los mismos filtros): restan de las ventas netas
        BigDecimal totalNotasCredito = BigDecimal.ZERO;
        for (NotaCredito nc : notaCreditoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)) {
            if (!filtro.test(nc.getPedido())) {
                continue;
            }
            totalNotasCredito = totalNotasCredito.add(nc.getTotal());
            totalVentas = totalVentas.subtract(nc.getTotal());
            resumenMetodos.merge(nc.getPedido().getMetodoPago(), nc.getTotal().negate(), BigDecimal::add);
        }
        reporte.setTotalNotasCredito(totalNotasCredito);

        reporte.setTotalVentas(totalVentas);
        reporte.setCantidadVentas(cantidadVentas);
        reporte.setResumenMetodosPago(resumenMetodos);
        reporte.setPedidos(listaDTO);

        return reporte;
    }

    @Transactional(readOnly = true)
    public LibroVentasIvaDTO obtenerLibroVentasIva(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        // Las facturas anuladas se listan (con importes en cero) para que el correlativo no muestre huecos;
        // los tickets no fiscales cancelados no van al libro.
        List<Pedido> pedidos = pedidoRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin).stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO || (p.getTipoComprobante() == TipoComprobante.FACTURA && p.getNumeroFactura() != null))
                .toList();
        int comprobantesVigentes = 0;

        LibroVentasIvaDTO libro = new LibroVentasIvaDTO();
        libro.setPeriodoInicio(inicio);
        libro.setPeriodoFin(fin);

        BigDecimal totalExentas = BigDecimal.ZERO;
        BigDecimal totalGravada5 = BigDecimal.ZERO;
        BigDecimal totalIva5 = BigDecimal.ZERO;
        BigDecimal totalGravada10 = BigDecimal.ZERO;
        BigDecimal totalIva10 = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal granTotal = BigDecimal.ZERO;

        List<ItemLibroIvaDTO> items = new ArrayList<>();

        for (Pedido p : pedidos) {
            boolean anulada = p.getEstado() == EstadoPedido.CANCELADO;
            ItemLibroIvaDTO item = new ItemLibroIvaDTO();
            item.setAnulada(anulada);
            item.setPedidoId(p.getId());
            item.setFecha(p.getFechaCreacion());
            item.setTipoComprobante(p.getTipoComprobante());
            item.setCondicionVenta(p.getCondicionVenta());
            item.setNumeroComprobante(p.getNumeroFactura() != null ? p.getNumeroFactura() : "TK-" + p.getId());
            item.setClienteRuc(p.getClienteRuc() != null && !p.getClienteRuc().isBlank() ? p.getClienteRuc() : "44444401-7");
            item.setClienteNombre(p.getClienteNombre() != null && !p.getClienteNombre().isBlank() ? p.getClienteNombre() : "Consumidor Final");

            if (anulada) {
                // Anulada: figura en el libro con importes en cero
                item.setClienteRuc("-");
                item.setClienteNombre("ANULADA");
                items.add(item);
                continue;
            }
            comprobantesVigentes++;
            BigDecimal exentas = p.getTotalExentas() != null ? p.getTotalExentas() : BigDecimal.ZERO;
            BigDecimal grav5 = p.getTotalGravada5() != null ? p.getTotalGravada5() : BigDecimal.ZERO;
            BigDecimal iva5 = p.getTotalIva5() != null ? p.getTotalIva5() : BigDecimal.ZERO;
            BigDecimal grav10 = p.getTotalGravada10() != null ? p.getTotalGravada10() : BigDecimal.ZERO;
            BigDecimal iva10 = p.getTotalIva10() != null ? p.getTotalIva10() : BigDecimal.ZERO;
            BigDecimal totIva = p.getTotalIva() != null ? p.getTotalIva() : BigDecimal.ZERO;
            BigDecimal tot = MonedaPyUtils.redondearGs(p.getTotal());

            item.setExentas(exentas);
            item.setGravada5(grav5);
            item.setIva5(iva5);
            item.setGravada10(grav10);
            item.setIva10(iva10);
            item.setTotalIva(totIva);
            item.setTotal(tot);

            items.add(item);

            totalExentas = totalExentas.add(exentas);
            totalGravada5 = totalGravada5.add(grav5);
            totalIva5 = totalIva5.add(iva5);
            totalGravada10 = totalGravada10.add(grav10);
            totalIva10 = totalIva10.add(iva10);
            totalIva = totalIva.add(totIva);
            granTotal = granTotal.add(tot);
        }

        // Las notas de crédito del período restan: entran al libro con importes negativos
        for (NotaCredito nc : notaCreditoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)) {
            Pedido f = nc.getPedido();
            ItemLibroIvaDTO item = new ItemLibroIvaDTO();
            item.setPedidoId(f.getId());
            item.setFecha(nc.getFecha());
            item.setTipoComprobante(TipoComprobante.NOTA_CREDITO);
            item.setCondicionVenta(f.getCondicionVenta());
            item.setNumeroComprobante(nc.getNumero());
            item.setClienteRuc(f.getClienteRuc() != null && !f.getClienteRuc().isBlank() ? f.getClienteRuc() : "44444401-7");
            item.setClienteNombre(f.getClienteNombre() != null && !f.getClienteNombre().isBlank() ? f.getClienteNombre() : "Consumidor Final");
            item.setExentas(nc.getTotalExentas().negate());
            item.setGravada5(nc.getTotalGravada5().negate());
            item.setIva5(nc.getTotalIva5().negate());
            item.setGravada10(nc.getTotalGravada10().negate());
            item.setIva10(nc.getTotalIva10().negate());
            item.setTotalIva(nc.getTotalIva().negate());
            item.setTotal(nc.getTotal().negate());
            items.add(item);

            totalExentas = totalExentas.add(item.getExentas());
            totalGravada5 = totalGravada5.add(item.getGravada5());
            totalIva5 = totalIva5.add(item.getIva5());
            totalGravada10 = totalGravada10.add(item.getGravada10());
            totalIva10 = totalIva10.add(item.getIva10());
            totalIva = totalIva.add(item.getTotalIva());
            granTotal = granTotal.add(item.getTotal());
            comprobantesVigentes++;
        }
        items.sort(java.util.Comparator.comparing(ItemLibroIvaDTO::getFecha, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));

        libro.setTotalExentas(totalExentas);
        libro.setTotalGravada5(totalGravada5);
        libro.setTotalIva5(totalIva5);
        libro.setTotalGravada10(totalGravada10);
        libro.setTotalIva10(totalIva10);
        libro.setTotalIva(totalIva);
        libro.setGranTotal(granTotal);
        libro.setCantidadComprobantes(comprobantesVigentes);
        libro.setItems(items);

        return libro;
    }

    @Transactional(readOnly = true)
    public byte[] generarLibroVentasCsv(LocalDate fechaInicio, LocalDate fechaFin) {
        LibroVentasIvaDTO libro = obtenerLibroVentasIva(fechaInicio, fechaFin);

        StringBuilder sb = new StringBuilder();
        // BOM UTF-8 para apertura directa en Microsoft Excel en español
        sb.append('\uFEFF');

        // Encabezado CSV con separador punto y coma (;)
        sb.append("Fecha;Tipo Comprobante;Condicion;Numero Comprobante;RUC / C.I.;Cliente / Razon Social;Exentas (Gs.);Gravadas 5% (Gs.);IVA 5% (Gs.);Gravadas 10% (Gs.);IVA 10% (Gs.);Total IVA (Gs.);Total Comprobante (Gs.);Estado\r\n");

        for (ItemLibroIvaDTO item : libro.getItems()) {
            sb.append(item.getFecha() != null ? FORMATO_FECHA_HORA.format(item.getFecha()) : "").append(";")
                    .append(item.getTipoComprobante()).append(";")
                    .append(item.getCondicionVenta() != null ? item.getCondicionVenta() : "").append(";")
                    .append(escaparCsv(item.getNumeroComprobante())).append(";")
                    .append(escaparCsv(item.getClienteRuc())).append(";")
                    .append(escaparCsv(item.getClienteNombre())).append(";")
                    .append(item.getExentas()).append(";")
                    .append(item.getGravada5()).append(";")
                    .append(item.getIva5()).append(";")
                    .append(item.getGravada10()).append(";")
                    .append(item.getIva10()).append(";")
                    .append(item.getTotalIva()).append(";")
                    .append(item.getTotal()).append(";")
                    .append(item.isAnulada() ? "ANULADA" : "EMITIDA").append("\r\n");
        }

        // Fila de resumen total
        sb.append("TOTALES;;;;;;")
                .append(libro.getTotalExentas()).append(";")
                .append(libro.getTotalGravada5()).append(";")
                .append(libro.getTotalIva5()).append(";")
                .append(libro.getTotalGravada10()).append(";")
                .append(libro.getTotalIva10()).append(";")
                .append(libro.getTotalIva()).append(";")
                .append(libro.getGranTotal()).append(";\r\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public ReporteCajasDTO obtenerReporteCajas(LocalDate fechaInicio, LocalDate fechaFin, String usuario) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        List<SesionCaja> sesiones;
        if (usuario != null && !usuario.isBlank()) {
            sesiones = sesionCajaRepository.findByUsuarioApertura_UsernameAndFechaAperturaBetweenOrderByFechaAperturaDesc(usuario.trim(), inicio, fin);
        } else {
            sesiones = sesionCajaRepository.findByFechaAperturaBetweenOrderByFechaAperturaDesc(inicio, fin);
        }

        ReporteCajasDTO reporte = new ReporteCajasDTO();
        reporte.setPeriodoInicio(inicio);
        reporte.setPeriodoFin(fin);

        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalSobrante = BigDecimal.ZERO;
        BigDecimal totalFaltante = BigDecimal.ZERO;
        List<SesionCajaReporteDTO> listaDTO = new ArrayList<>();

        for (SesionCaja s : sesiones) {
            SesionCajaReporteDTO item = new SesionCajaReporteDTO();
            item.setId(s.getId());
            item.setCajeroApertura(s.getUsuarioApertura() != null ? s.getUsuarioApertura().getUsername() : null);
            item.setUsuarioCierre(s.getUsuarioCierre() != null ? s.getUsuarioCierre().getUsername() : null);
            item.setFechaApertura(s.getFechaApertura());
            item.setFechaCierre(s.getFechaCierre());
            item.setMontoInicial(s.getMontoInicial());
            item.setMontoEsperadoEfectivo(s.getMontoEsperadoEfectivo());
            item.setMontoRealEfectivo(s.getMontoRealEfectivo());
            item.setDiferencia(s.getDiferencia());
            item.setTotalVentasGeneral(s.getTotalVentasGeneral() != null ? s.getTotalVentasGeneral() : BigDecimal.ZERO);
            item.setCantidadPedidos(s.getCantidadPedidos());
            item.setEstado(s.getEstado());

            listaDTO.add(item);

            if (item.getTotalVentasGeneral() != null) {
                totalVentas = totalVentas.add(item.getTotalVentasGeneral());
            }

            if (s.getDiferencia() != null) {
                if (s.getDiferencia().compareTo(BigDecimal.ZERO) > 0) {
                    totalSobrante = totalSobrante.add(s.getDiferencia());
                } else if (s.getDiferencia().compareTo(BigDecimal.ZERO) < 0) {
                    totalFaltante = totalFaltante.add(s.getDiferencia().abs());
                }
            }
        }

        reporte.setTotalSesiones(sesiones.size());
        reporte.setTotalVentasGeneral(totalVentas);
        reporte.setTotalSobrante(totalSobrante);
        reporte.setTotalFaltante(totalFaltante);
        reporte.setSesiones(listaDTO);

        return reporte;
    }

    @Transactional(readOnly = true)
    public RankingProductosDTO obtenerRankingProductos(LocalDate fechaInicio, LocalDate fechaFin, int limite) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
        int max = limite > 0 ? limite : 10;

        List<Pedido> pedidos = pedidoRepository.findByFechaCreacionBetweenAndEstadoNotOrderByFechaCreacionDesc(inicio, fin, EstadoPedido.CANCELADO);

        RankingProductosDTO ranking = new RankingProductosDTO();
        ranking.setPeriodoInicio(inicio);
        ranking.setPeriodoFin(fin);

        Map<Long, ItemRankingDTO> presentacionesMap = new HashMap<>();
        Map<Long, ItemRankingDTO> simplesMap = new HashMap<>();
        Map<Long, SaborRankingDTO> saboresMap = new HashMap<>();
        Map<Long, ItemRankingDTO> toppingsMap = new HashMap<>();

        for (Pedido p : pedidos) {
            if (p.getDetalles() == null) continue;
            for (DetallePedido d : p.getDetalles()) {
                if (d.getTipoItem() == TipoItemPedido.HELADO && d.getPresentacion() != null) {
                    ItemRankingDTO ir = presentacionesMap.computeIfAbsent(d.getPresentacion().getId(), id ->
                            new ItemRankingDTO(id, d.getPresentacion().getNombre(), "HELADO", 0, BigDecimal.ZERO));
                    ir.setCantidad(ir.getCantidad() + d.getCantidad());
                    ir.setTotalRecaudado(ir.getTotalRecaudado().add(d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO));

                    if (d.getSabores() != null && !d.getSabores().isEmpty()) {
                        double kilos = 0.0;
                        if (d.getPresentacion().getPesoGramosAprox() != null && d.getPresentacion().getPesoGramosAprox() > 0) {
                            kilos = (((double) d.getPresentacion().getPesoGramosAprox() * d.getCantidad())
                                    / (1000.0 * d.getSabores().size()));
                        }
                        for (Sabor s : d.getSabores()) {
                            SaborRankingDTO sr = saboresMap.computeIfAbsent(s.getId(), id ->
                                    new SaborRankingDTO(id, s.getNombre(), s.getCategoria() != null ? s.getCategoria().name() : null, 0, 0.0));
                            sr.setCantidadElecciones(sr.getCantidadElecciones() + d.getCantidad());
                            sr.setKilosEstimados(Math.round((sr.getKilosEstimados() + kilos) * 100.0) / 100.0);
                        }
                    }

                    if (d.getToppings() != null) {
                        for (Topping t : d.getToppings()) {
                            ItemRankingDTO tr = toppingsMap.computeIfAbsent(t.getId(), id ->
                                    new ItemRankingDTO(id, t.getNombre(), "TOPPING", 0, BigDecimal.ZERO));
                            tr.setCantidad(tr.getCantidad() + d.getCantidad());
                            if (t.getPrecioExtra() != null) {
                                tr.setTotalRecaudado(tr.getTotalRecaudado().add(t.getPrecioExtra().multiply(BigDecimal.valueOf(d.getCantidad()))));
                            }
                        }
                    }
                } else if (d.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && d.getProductoSimple() != null) {
                    ItemRankingDTO ir = simplesMap.computeIfAbsent(d.getProductoSimple().getId(), id ->
                            new ItemRankingDTO(id, d.getProductoSimple().getNombre(), "PRODUCTO_SIMPLE", 0, BigDecimal.ZERO));
                    ir.setCantidad(ir.getCantidad() + d.getCantidad());
                    ir.setTotalRecaudado(ir.getTotalRecaudado().add(d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO));
                } else if (d.getTipoItem() == TipoItemPedido.TOPPING && d.getToppings() != null) {
                    for (Topping t : d.getToppings()) {
                        ItemRankingDTO tr = toppingsMap.computeIfAbsent(t.getId(), id ->
                                new ItemRankingDTO(id, t.getNombre(), "TOPPING", 0, BigDecimal.ZERO));
                        tr.setCantidad(tr.getCantidad() + d.getCantidad());
                        tr.setTotalRecaudado(tr.getTotalRecaudado().add(d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO));
                    }
                }
            }
        }

        ranking.setTopPresentaciones(presentacionesMap.values().stream()
                .sorted(Comparator.comparingLong(ItemRankingDTO::getCantidad).reversed())
                .limit(max).toList());

        ranking.setTopProductosSimples(simplesMap.values().stream()
                .sorted(Comparator.comparingLong(ItemRankingDTO::getCantidad).reversed())
                .limit(max).toList());

        ranking.setTopSabores(saboresMap.values().stream()
                .sorted(Comparator.comparingLong(SaborRankingDTO::getCantidadElecciones).reversed())
                .limit(max).toList());

        ranking.setTopToppings(toppingsMap.values().stream()
                .sorted(Comparator.comparingLong(ItemRankingDTO::getCantidad).reversed())
                .limit(max).toList());

        return ranking;
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
