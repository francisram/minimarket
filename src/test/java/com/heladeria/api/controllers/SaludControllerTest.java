package com.heladeria.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Healthcheck de la API")
class SaludControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("/api/salud responde 200 sin token cuando la base contesta")
    void salud() throws Exception {
        mockMvc.perform(get("/api/salud")).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("UP"));
    }
}
