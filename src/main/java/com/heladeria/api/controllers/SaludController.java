package com.heladeria.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Estado de la API para el healthcheck de Docker: responde 200 solo si la base de datos contesta. */
@RestController
@RequestMapping("/api/salud")
public class SaludController {

    private final JdbcTemplate jdbc;

    public SaludController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> estado() {
        try {
            jdbc.queryForObject("select 1", Integer.class);
            return ResponseEntity.ok(Map.of("estado", "UP"));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("estado", "DOWN"));
        }
    }
}
