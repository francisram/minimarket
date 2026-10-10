package com.heladeria.api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Configuración global de serialización JSON de Jackson para Guaraníes Paraguayos (PYG).
 * En Paraguay la moneda no utiliza centavos ni decimales; los precios y totales se
 * serializan como números enteros (ej. 12000 en lugar de 12000.00).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizarJacksonParaMonedaPy() {
        return builder -> {
            builder.failOnEmptyBeans(false);
            builder.serializerByType(BigDecimal.class, new JsonSerializer<BigDecimal>() {
                @Override
                public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value == null) {
                        gen.writeNull();
                    } else {
                        // Escribir como entero sin decimales
                        gen.writeNumber(value.setScale(0, RoundingMode.HALF_UP).toBigInteger());
                    }
                }
            });
        };
    }
}
