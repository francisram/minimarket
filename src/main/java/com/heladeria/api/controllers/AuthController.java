package com.heladeria.api.controllers;

import com.heladeria.api.dto.CambiarPasswordDTO;
import com.heladeria.api.dto.LoginRequestDTO;
import com.heladeria.api.dto.LoginResponseDTO;
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
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña cuando está expirada o requiere cambio forzado")
    public ResponseEntity<LoginResponseDTO> cambiarPassword(@Valid @RequestBody CambiarPasswordDTO dto) {
        LoginResponseDTO response = authService.cambiarPassword(dto);
        return ResponseEntity.ok(response);
    }
}
