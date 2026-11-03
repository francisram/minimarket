package com.heladeria.api.controllers;

import com.heladeria.api.dto.CambiarPasswordDTO;
import com.heladeria.api.dto.LoginRequestDTO;
import com.heladeria.api.dto.LoginResponseDTO;
import com.heladeria.api.dto.RefreshTokenRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import com.heladeria.api.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Autenticación", description = "Endpoints de login y cambio de contraseña")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener Bearer token JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto, HttpServletRequest request) {
        LoginResponseDTO response = authService.login(dto, dispositivo(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña cuando está expirada o requiere cambio forzado")
    public ResponseEntity<LoginResponseDTO> cambiarPassword(@Valid @RequestBody CambiarPasswordDTO dto, HttpServletRequest request) {
        LoginResponseDTO response = authService.cambiarPassword(dto, dispositivo(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el token de acceso con el token de refresco (que se rota)")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        return ResponseEntity.ok(authService.refrescar(dto.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión: invalida en el servidor el token de refresco y su sesión")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        authService.cerrarSesion(dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sesiones/{username}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @Operation(summary = "Cerrar todas las sesiones abiertas de un usuario (solo ADMIN/OWNER)")
    public ResponseEntity<java.util.Map<String, Integer>> cerrarSesionesDe(@PathVariable String username) {
        return ResponseEntity.ok(java.util.Map.of("sesionesCerradas", authService.cerrarSesionesDe(username)));
    }

    private static String dispositivo(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
