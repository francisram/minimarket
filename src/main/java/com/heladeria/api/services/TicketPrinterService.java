package com.heladeria.api.services;

import com.heladeria.api.dto.TicketEmitidoDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.exceptions.ImpresionException;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.repositories.InstitucionRepository;
import com.heladeria.api.repositories.TicketeraRepository;
import com.heladeria.api.util.MonedaPyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class TicketPrinterService {

    private static final Logger log = LoggerFactory.getLogger(TicketPrinterService.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final byte[] INICIALIZAR = {0x1B, 0x40};
    private static final byte[] ALINEAR_CENTRO = {0x1B, 0x61, 0x01};
    private static final byte[] ALINEAR_IZQUIERDA = {0x1B, 0x61, 0x00};
    private static final byte[] TEXTO_DOBLE = {0x1D, 0x21, 0x11};
    private static final byte[] TEXTO_NORMAL = {0x1D, 0x21, 0x00};
    private static final byte[] AVANZAR_Y_CORTAR = {0x1B, 0x64, 0x04, 0x1D, 0x56, 0x00};

    private final TicketeraRepository ticketeraRepository;
    private final ImpresoraRepository impresoraRepository;
    private final InstitucionRepository institucionRepository;
    private final ImpresionService impresionService;

    public TicketPrinterService(TicketeraRepository ticketeraRepository,
                                ImpresoraRepository impresoraRepository,
                                InstitucionRepository institucionRepository,
                                ImpresionService impresionService) {
        this.ticketeraRepository = ticketeraRepository;
        this.impresoraRepository = impresoraRepository;
        this.institucionRepository = institucionRepository;
        this.impresionService = impresionService;
    }

    public void imprimirEnSegundoPlano(Long ticketeraId, TicketEmitidoDTO ticket) {
        if (ticketeraId == null) {
            return;
        }
        Thread.ofVirtual().start(() -> imprimir(ticketeraId, ticket));
    }

    private void imprimir(Long ticketeraId, TicketEmitidoDTO ticket) {
        try {
            Optional<Ticketera> ticketera = ticketeraRepository.findById(ticketeraId);
            if (ticketera.isEmpty()) {
                log.warn("ticketeraId {} no existe, no se imprime el ticket {}", ticketeraId, ticket.ticket());
                return;
            }
            Long impresoraId = ticketera.get().getImpresoraId();
            Optional<Impresora> impresora = impresoraRepository.findById(impresoraId);
            if (impresora.isEmpty()) {
                log.warn("impresoraId {} de la ticketera {} no existe, no se imprime el ticket {}",
                        impresoraId, ticketeraId, ticket.ticket());
                return;
            }
            try {
                impresionService.imprimir(impresora.get(), construirTicket(ticket));
            } catch (ImpresionException e) {
                log.warn("No se pudo imprimir el ticket {}: {}", ticket.ticket(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error inesperado al intentar imprimir el ticket {}", ticket.ticket(), e);
        }
    }

    public void imprimirPedidoEnSegundoPlano(Long impresoraId, Pedido pedido) {
        if (pedido == null) {
            return;
        }
        Thread.ofVirtual().start(() -> imprimirPedido(impresoraId, pedido));
    }

    public void imprimirPedido(Long impresoraId, Pedido pedido) {
        if (pedido == null) {
            return;
        }
        try {
            Optional<Impresora> impresora;
            if (impresoraId != null) {
                impresora = impresoraRepository.findById(impresoraId);
                if (impresora.isEmpty()) {
                    log.warn("impresoraId {} no existe, no se imprime el ticket del pedido #{}", impresoraId, pedido.getId());
                    return;
                }
            } else {
                impresora = impresoraRepository.findAll().stream().findFirst();
                if (impresora.isEmpty()) {
                    log.warn("No hay ninguna impresora configurada, no se imprime el ticket del pedido #{}", pedido.getId());
                    return;
                }
            }

            try {
                impresionService.imprimir(impresora.get(), construirTicketPedido(pedido));
                log.info("Ticket del pedido #{} enviado a imprimir en '{}'", pedido.getId(), impresora.get().getNombre());
            } catch (ImpresionException e) {
                log.warn("No se pudo imprimir el ticket del pedido #{}: {}", pedido.getId(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error inesperado al intentar imprimir el ticket del pedido #{}", pedido.getId(), e);
        }
    }

    public byte[] construirTicketPedido(Pedido pedido) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(INICIALIZAR);
        out.writeBytes(ALINEAR_CENTRO);
        logoInstitucion().ifPresent(out::writeBytes);

        String nombreLocal = institucionRepository.findById(1L)
                .map(Institucion::getNombre)
                .filter(n -> !n.isBlank())
                .orElse("HELADERIA ARTESANAL");
        escribirLinea(out, nombreLocal);

        out.writeBytes(TEXTO_DOBLE);
        escribirLinea(out, "PEDIDO #" + pedido.getId());
        out.writeBytes(TEXTO_NORMAL);

        if (pedido.getFechaCreacion() != null) {
            escribirLinea(out, FORMATO_FECHA.format(pedido.getFechaCreacion()));
        }

        out.writeBytes(ALINEAR_IZQUIERDA);
        escribirLinea(out, "--------------------------------");
        if (pedido.getClienteNombre() != null && !pedido.getClienteNombre().isBlank()) {
            escribirLinea(out, "Cliente: " + pedido.getClienteNombre());
        }
        if (pedido.getMetodoPago() != null) {
            escribirLinea(out, "Pago: " + pedido.getMetodoPago());
        }
        if (pedido.getTipoEntrega() != null) {
            escribirLinea(out, "Entrega: " + pedido.getTipoEntrega());
        }
        escribirLinea(out, "--------------------------------");
        escribirLinea(out, "CANT DESCRIPCION           TOTAL");
        escribirLinea(out, "--------------------------------");

        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                String desc = "";
                if (d.getTipoItem() == TipoItemPedido.HELADO && d.getPresentacion() != null) {
                    desc = d.getPresentacion().getNombre();
                } else if (d.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && d.getProductoSimple() != null) {
                    desc = d.getProductoSimple().getNombre();
                } else {
                    desc = "Item";
                }

                escribirLinea(out, formatearLineaItem(d.getCantidad(), desc, MonedaPyUtils.formatearGs(d.getSubtotal())));

                if (d.getSabores() != null && !d.getSabores().isEmpty()) {
                    for (Sabor s : d.getSabores()) {
                        escribirLinea(out, "  * " + s.getNombre());
                    }
                }
                if (d.getToppings() != null && !d.getToppings().isEmpty()) {
                    for (Topping t : d.getToppings()) {
                        escribirLinea(out, "  + " + t.getNombre() + " (" + MonedaPyUtils.formatearGs(t.getPrecioExtra()) + ")");
                    }
                }
            }
        }

        escribirLinea(out, "--------------------------------");
        out.writeBytes(TEXTO_DOBLE);
        escribirLinea(out, "TOTAL: " + MonedaPyUtils.formatearGs(pedido.getTotal()));
        out.writeBytes(TEXTO_NORMAL);
        escribirLinea(out, "--------------------------------");

        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        BigDecimal gravada10 = MonedaPyUtils.calcularGravada10(total);
        BigDecimal iva10 = MonedaPyUtils.calcularIva10(total);

        escribirLinea(out, "LIQUIDACION DE IVA");
        escribirLinea(out, alinearDosColumnas("Gravadas 10%:", MonedaPyUtils.formatearGs(gravada10)));
        escribirLinea(out, alinearDosColumnas("IVA 10%:", MonedaPyUtils.formatearGs(iva10)));
        escribirLinea(out, alinearDosColumnas("Total IVA:", MonedaPyUtils.formatearGs(iva10)));
        escribirLinea(out, "--------------------------------");

        if (pedido.getNotas() != null && !pedido.getNotas().isBlank()) {
            escribirLinea(out, "Notas: " + pedido.getNotas());
            escribirLinea(out, "--------------------------------");
        }

        out.writeBytes(ALINEAR_CENTRO);
        escribirLinea(out, "Gracias por su preferencia!");
        out.writeBytes(AVANZAR_Y_CORTAR);

        return out.toByteArray();
    }

    private String formatearLineaItem(int cant, String desc, String subtotal) {
        String cantStr = cant + "x ";
        int espacioDesc = 32 - cantStr.length() - subtotal.length() - 1;
        if (espacioDesc < 4) {
            espacioDesc = 4;
        }
        String descCorta = desc.length() > espacioDesc ? desc.substring(0, espacioDesc) : desc;
        int espaciosRelleno = 32 - cantStr.length() - descCorta.length() - subtotal.length();
        if (espaciosRelleno < 1) {
            espaciosRelleno = 1;
        }
        return cantStr + descCorta + " ".repeat(espaciosRelleno) + subtotal;
    }

    private String alinearDosColumnas(String etiqueta, String valor) {
        int relleno = 32 - etiqueta.length() - valor.length();
        if (relleno < 1) {
            relleno = 1;
        }
        return etiqueta + " ".repeat(relleno) + valor;
    }

    private byte[] construirTicket(TicketEmitidoDTO ticket) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(INICIALIZAR);
        out.writeBytes(ALINEAR_CENTRO);
        logoInstitucion().ifPresent(out::writeBytes);
        out.writeBytes(TEXTO_DOBLE);
        escribirLinea(out, ticket.ticket());
        out.writeBytes(TEXTO_NORMAL);
        if (ticket.preferencial()) {
            escribirLinea(out, "** PREFERENCIAL **");
        }
        if (ticket.lugar() != null) {
            escribirLinea(out, "Lugar: " + ticket.lugar());
        }
        out.writeBytes(ALINEAR_IZQUIERDA);
        if (ticket.socio() != null) {
            escribirLinea(out, (ticket.visitante() ? "Visitante: " : "Socio: ") + ticket.socio());
        }
        escribirLinea(out, FORMATO_FECHA.format(ticket.timestamp().atZoneSameInstant(ZoneId.systemDefault())));
        out.writeBytes(AVANZAR_Y_CORTAR);
        return out.toByteArray();
    }

    private void escribirLinea(ByteArrayOutputStream out, String texto) {
        out.writeBytes((texto + "\n").getBytes(StandardCharsets.ISO_8859_1));
    }

    private Optional<byte[]> logoInstitucion() {
        return institucionRepository.findById(1L)
                .map(Institucion::getLogoBase64)
                .flatMap(impresionService::logoRaster);
    }
}
