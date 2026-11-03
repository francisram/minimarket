package com.heladeria.api.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Cifra al guardar y descifra al leer: el resto del código sigue trabajando con el texto original. */
@Converter
@Component
public class SecretoCifradoConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(SecretoCifradoConverter.class);
    private final CifradoService cifrado;

    public SecretoCifradoConverter(CifradoService cifrado) {
        this.cifrado = cifrado;
    }

    @Override
    public String convertToDatabaseColumn(String atributo) {
        return cifrado.cifrar(atributo);
    }

    @Override
    public String convertToEntityAttribute(String columna) {
        try {
            return cifrado.descifrar(columna);
        } catch (IllegalStateException e) {
            // Clave distinta o dato alterado: no se cae la carga de la entidad; la impresora fallará al autenticar y se reingresa la clave
            log.error("No se pudo descifrar un secreto guardado ({}). Verifique APP_CIFRADO_CLAVE.", e.getMessage());
            return null;
        }
    }
}
