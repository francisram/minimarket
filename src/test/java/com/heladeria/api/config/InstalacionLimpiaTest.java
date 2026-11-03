package com.heladeria.api.config;

import com.heladeria.api.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/** Una instalación nueva arranca con lo imprescindible y sin datos de demostración. */
@SpringBootTest(properties = {
        "app.seed.datos-demo=false",
        // base propia: no comparte la en memoria (con datos de demostración) de las demás pruebas
        "spring.datasource.url=jdbc:h2:mem:instalacion_limpia;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@DisplayName("Instalación limpia (sin datos de demostración)")
class InstalacionLimpiaTest {

    @Autowired private SaborRepository saborRepository;
    @Autowired private PresentacionRepository presentacionRepository;
    @Autowired private ToppingRepository toppingRepository;
    @Autowired private ProductoSimpleRepository productoSimpleRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PuntoExpedicionRepository puntoExpedicionRepository;
    @Autowired private InstitucionRepository institucionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PaginaRepository paginaRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    @Test
    @DisplayName("No hay sabores, productos, proveedores, compras ni clientes de ejemplo")
    void sinDatosDeEjemplo() {
        assertEquals(0, saborRepository.count());
        assertEquals(0, presentacionRepository.count());
        assertEquals(0, toppingRepository.count());
        assertEquals(0, productoSimpleRepository.count());
        assertEquals(0, productoRepository.count());
        assertEquals(0, proveedorRepository.count());
        assertEquals(0, compraRepository.count());
        assertEquals(1, clienteRepository.count(), "solo 'Consumidor Final'");
        assertEquals("Consumidor Final", clienteRepository.findAll().get(0).getRazonSocial());
    }

    @Test
    @DisplayName("Quedan los roles, los usuarios admin y owner, las páginas y las categorías")
    void loImprescindibleSiEsta() {
        assertTrue(rolRepository.findByNombreRol("ADMIN").isPresent());
        assertTrue(rolRepository.findByNombreRol("OWNER").isPresent());
        assertTrue(rolRepository.findByNombreRol("CAJERO").isPresent());
        assertTrue(usuarioRepository.findByUsername("admin").isPresent());
        assertTrue(usuarioRepository.findByUsername("owner").isPresent());
        assertTrue(paginaRepository.count() > 0);
        assertTrue(categoriaRepository.count() > 0);
    }

    @Test
    @DisplayName("Una sola caja, sin timbrado ni RUC ni datos de contacto inventados")
    void sinDatosFiscalesInventados() {
        assertEquals(1, puntoExpedicionRepository.count());
        var punto = puntoExpedicionRepository.findAll().get(0);
        assertNull(punto.getTimbrado());
        var inst = institucionRepository.findById(1L).orElseThrow();
        assertNull(inst.getRuc());
        assertNull(inst.getTimbrado());
        assertNull(inst.getDireccion());
        assertNull(inst.getTelefono());
    }
}
