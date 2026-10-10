package com.heladeria.api.dto;

import java.time.LocalDateTime;

public record ClienteDTO(
    Long id,
    String ruc,
    String razonSocial,
    String direccion,
    String telefono,
    String email,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {}
