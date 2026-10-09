package com.heladeria.api.dto;

import java.time.OffsetDateTime;

public record TicketEmitidoDTO(
        String ticket,
        String lugar,
        String socio,
        boolean visitante,
        boolean preferencial,
        OffsetDateTime timestamp) {
}
