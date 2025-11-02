package com.sanmartin.minimarketbase.controllers;

import com.sanmartin.minimarketbase.dto.AuthRequest;
import com.sanmartin.minimarketbase.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Temporalmente permitimos todos los orígenes
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        
        System.out.println("=== PETICIÓN DE LOGIN RECIBIDA ===");
        System.out.println("Usuario: " + authRequest.getUsername());
        System.out.println("Password: " + authRequest.getPassword());
        
        // Validación temporal (sin base de datos por ahora)
        if ("admin".equals(authRequest.getUsername()) && "1234".equals(authRequest.getPassword())) {
            // Simular respuesta exitosa
            AuthResponse response = new AuthResponse();
            response.setToken("jwt-token-simulado-" + System.currentTimeMillis());
            response.setId(1L);
            response.setUsername("admin");
            response.setEmail("admin@minimarket.com");
            response.setMessage("Login exitoso");
            
            System.out.println("Login exitoso para usuario: " + authRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } else {
            // Credenciales inválidas
            AuthResponse errorResponse = new AuthResponse("Credenciales inválidas");
            System.out.println("Login fallido para usuario: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    // Endpoint de prueba para verificar que el backend está funcionando
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        String message = "Backend Minimarket funcionando correctamente - " + System.currentTimeMillis();
        System.out.println(message);
        return ResponseEntity.ok(message);
    }
    
    // Endpoint para recibir cualquier petición y loguearla (útil para debug)
    @PostMapping("/debug")
    public ResponseEntity<String> debug(@RequestBody String body) {
        System.out.println("=== PETICIÓN DEBUG RECIBIDA ===");
        System.out.println("Body: " + body);
        System.out.println("Headers: "); // Puedes agregar los headers si necesitas
        return ResponseEntity.ok("Petición recibida: " + body);
    }
}