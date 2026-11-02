package com.heladeria.api.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de la anotación @RucValido con Jakarta Bean Validation")
class RucValidoAnnotationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static class DummyProveedorDto {
        @RucValido(obligatorio = true, permitirCedulaSinDv = false)
        private final String ruc;

        public DummyProveedorDto(String ruc) {
            this.ruc = ruc;
        }
    }

    private static class DummyClienteDto {
        @RucValido(obligatorio = true, permitirCedulaSinDv = true)
        private final String ruc;

        public DummyClienteDto(String ruc) {
            this.ruc = ruc;
        }
    }

    private static class DummyPedidoVentaDto {
        @RucValido(obligatorio = false, permitirCedulaSinDv = true)
        private final String ruc;

        public DummyPedidoVentaDto(String ruc) {
            this.ruc = ruc;
        }
    }

    @Test
    @DisplayName("Proveedor: RUC válido 80106020-6 pasa sin violaciones")
    void testProveedorRucValido() {
        DummyProveedorDto dto = new DummyProveedorDto("80106020-6");
        Set<ConstraintViolation<DummyProveedorDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Proveedor: RUC con DV erróneo o sin guion falla")
    void testProveedorRucInvalido() {
        DummyProveedorDto dtoDvMalo = new DummyProveedorDto("80106020-0");
        Set<ConstraintViolation<DummyProveedorDto>> violations1 = validator.validate(dtoDvMalo);
        assertEquals(1, violations1.size());

        DummyProveedorDto dtoSinDv = new DummyProveedorDto("80106020");
        Set<ConstraintViolation<DummyProveedorDto>> violations2 = validator.validate(dtoSinDv);
        assertEquals(1, violations2.size());

        DummyProveedorDto dtoVacio = new DummyProveedorDto("");
        Set<ConstraintViolation<DummyProveedorDto>> violations3 = validator.validate(dtoVacio);
        assertEquals(1, violations3.size());
    }

    @Test
    @DisplayName("Cliente: Admite tanto RUC formal 3669922-5 como cédula civil 3669922")
    void testClienteRucOCedula() {
        DummyClienteDto dtoRuc = new DummyClienteDto("3669922-5");
        assertTrue(validator.validate(dtoRuc).isEmpty());

        DummyClienteDto dtoCedula = new DummyClienteDto("3669922");
        assertTrue(validator.validate(dtoCedula).isEmpty());

        // Con guion pero DV incorrecto debe fallar
        DummyClienteDto dtoDvMalo = new DummyClienteDto("3669922-0");
        assertEquals(1, validator.validate(dtoDvMalo).size());
    }

    @Test
    @DisplayName("Pedido Venta: Opcional, permite null o vacío, y valida si se proporciona")
    void testPedidoVentaRucOpcional() {
        DummyPedidoVentaDto dtoNull = new DummyPedidoVentaDto(null);
        assertTrue(validator.validate(dtoNull).isEmpty());

        DummyPedidoVentaDto dtoVacio = new DummyPedidoVentaDto("   ");
        assertTrue(validator.validate(dtoVacio).isEmpty());

        DummyPedidoVentaDto dtoConsumidor = new DummyPedidoVentaDto("44444401-7");
        assertTrue(validator.validate(dtoConsumidor).isEmpty());

        DummyPedidoVentaDto dtoInvalido = new DummyPedidoVentaDto("12345-X");
        assertEquals(1, validator.validate(dtoInvalido).size());
    }
}
