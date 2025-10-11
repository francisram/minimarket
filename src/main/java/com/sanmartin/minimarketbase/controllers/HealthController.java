package com.sanmartin.minimarketbase.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final DatabaseTestService dbTest;

    public HealthController(DatabaseTestService dbTest) {
        this.dbTest = dbTest;
    }

    @GetMapping("/health")
    public String check() {
        return dbTest.testConnection()
                ? "✅ Conexión exitosa a PostgreSQL"
                : "❌ Error en la conexión a la base de datos";
    }
}
