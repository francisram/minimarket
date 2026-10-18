package com.heladeria.api.dto;

import java.time.LocalDateTime;

public record ProveedorDTO(
    Long id,
    String ruc,
    String razonSocial,
    String contacto,
    String direccion,
    String telefono,
    String email,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {}
