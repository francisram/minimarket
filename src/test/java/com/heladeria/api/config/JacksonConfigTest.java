package com.heladeria.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@DisplayName("Pruebas de serialización JSON para Guaraníes Paraguayos (JacksonConfig)")
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Los valores BigDecimal se serializan en JSON como enteros sin decimales")
    void testBigDecimalSerializaComoEnteroSinDecimales() throws Exception {
        Map<String, Object> data = Map.of(
                "precio", new BigDecimal("12000.00"),
                "total", new BigDecimal("45000.000"),
                "extra", new BigDecimal("3000")
        );

        String json = objectMapper.writeValueAsString(data);

        // No debe contener decimales .00 ni .000
        assertFalse(json.contains(".00"), "El JSON no debe contener '.00': " + json);
        assertFalse(json.contains(".0"), "El JSON no debe contener '.0': " + json);

        // Debe contener los enteros directos
        assertEquals(12000, objectMapper.readTree(json).get("precio").asInt());
        assertEquals(45000, objectMapper.readTree(json).get("total").asInt());
        assertEquals(3000, objectMapper.readTree(json).get("extra").asInt());
    }
}
