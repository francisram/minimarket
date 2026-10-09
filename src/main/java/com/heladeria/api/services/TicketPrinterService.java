package com.heladeria.api.services;

import com.heladeria.api.dto.TicketEmitidoDTO;
import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.Institucion;
import com.heladeria.api.entities.Ticketera;
import com.heladeria.api.exceptions.ImpresionException;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.repositories.InstitucionRepository;
import com.heladeria.api.repositories.TicketeraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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
