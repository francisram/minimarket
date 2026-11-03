package com.heladeria.api.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

/** Utilidades de identidad: nunca se asume un usuario por defecto si no hay autenticación. */
public final class AuthUtils {

    private AuthUtils() {
    }

    public static String requireUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BadCredentialsException("Se requiere un usuario autenticado para realizar esta operación.");
        }
        return authentication.getName();
    }
}
