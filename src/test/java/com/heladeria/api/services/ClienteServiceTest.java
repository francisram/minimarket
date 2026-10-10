package com.heladeria.api.services;

import com.heladeria.api.dto.ClienteDTO;
import com.heladeria.api.dto.ClienteRequestDTO;
import com.heladeria.api.entities.Cliente;
import com.heladeria.api.exceptions.ConflictException;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClienteServiceTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
        clienteRepository.flush();
    }

    @Test
    @DisplayName("Crear cliente exitoso guarda datos y fecha de creación")
    void testCrearClienteExitoso() {
        ClienteRequestDTO req = new ClienteRequestDTO("1234567-8", "Juan Pérez", "Calle Palma 123", "0981111222", "juan@test.com", true);
        ClienteDTO creado = clienteService.crear(req);

        assertNotNull(creado.id());
        assertEquals("1234567-8", creado.ruc());
        assertEquals("Juan Pérez", creado.razonSocial());
        assertEquals("Calle Palma 123", creado.direccion());
        assertEquals("0981111222", creado.telefono());
        assertEquals("juan@test.com", creado.email());
        assertTrue(creado.activo());
        assertNotNull(creado.fechaCreacion());
    }

    @Test
    @DisplayName("Crear cliente con RUC duplicado arroja ConflictException")
    void testCrearClienteConRucDuplicadoLanzaConflictException() {
        ClienteRequestDTO req1 = new ClienteRequestDTO("80099999-0", "Empresa A", null, null, null, true);
        clienteService.crear(req1);

        ClienteRequestDTO req2 = new ClienteRequestDTO("80099999-0", "Empresa B", null, null, null, true);
        assertThrows(ConflictException.class, () -> clienteService.crear(req2));
    }

    @Test
    @DisplayName("Actualizar cliente modifica datos y establece fechaActualizacion")
    void testActualizarClienteExitoso() {
        ClienteRequestDTO req = new ClienteRequestDTO("2345678-9", "Ana Gómez", "Iturbe 456", "0971222333", null, true);
        ClienteDTO creado = clienteService.crear(req);

        ClienteRequestDTO updateReq = new ClienteRequestDTO("2345678-9", "Ana Gómez Actualizada", "Iturbe 789", "0971999888", "ana@test.com", true);
        ClienteDTO actualizado = clienteService.actualizar(creado.id(), updateReq);

        assertEquals("Ana Gómez Actualizada", actualizado.razonSocial());
        assertEquals("Iturbe 789", actualizado.direccion());
        assertEquals("0971999888", actualizado.telefono());
        assertEquals("ana@test.com", actualizado.email());
        assertNotNull(actualizado.fechaActualizacion());
    }

    @Test
    @DisplayName("Actualizar cliente con RUC ya perteneciente a otro cliente arroja ConflictException")
    void testActualizarClienteConRucDeOtroLanzaConflictException() {
        ClienteDTO c1 = clienteService.crear(new ClienteRequestDTO("1111111-1", "Cliente Uno", null, null, null, true));
        ClienteDTO c2 = clienteService.crear(new ClienteRequestDTO("2222222-2", "Cliente Dos", null, null, null, true));

        ClienteRequestDTO conflicto = new ClienteRequestDTO("1111111-1", "Cliente Dos Modificado", null, null, null, true);
        assertThrows(ConflictException.class, () -> clienteService.actualizar(c2.id(), conflicto));
    }

    @Test
    @DisplayName("Cambiar estado activa y desactiva al cliente")
    void testCambiarEstado() {
        ClienteDTO creado = clienteService.crear(new ClienteRequestDTO("3333333-3", "Cliente Tres", null, null, null, true));
        assertTrue(creado.activo());

        ClienteDTO desactivado = clienteService.cambiarEstado(creado.id(), false);
        assertFalse(desactivado.activo());
        assertNotNull(desactivado.fechaActualizacion());
    }

    @Test
    @DisplayName("Buscar por RUC encuentra al cliente ignorando mayúsculas y espacios")
    void testBuscarPorRuc() {
        clienteService.crear(new ClienteRequestDTO("80098765-4", "Distribuidora Central S.R.L.", null, null, null, true));

        Optional<ClienteDTO> encontrado = clienteService.buscarPorRuc(" 80098765-4 ");
        assertTrue(encontrado.isPresent());
        assertEquals("Distribuidora Central S.R.L.", encontrado.get().razonSocial());

        Optional<ClienteDTO> inexistente = clienteService.buscarPorRuc("9999999-9");
        assertTrue(inexistente.isEmpty());
    }

    @Test
    @DisplayName("Listar con filtro busca por coincidencia en RUC o Razón Social")
    void testListarConFiltro() {
        clienteService.crear(new ClienteRequestDTO("123456-1", "Helados del Este", null, null, null, true));
        clienteService.crear(new ClienteRequestDTO("789101-2", "Supermercado Central", null, null, null, true));
        clienteService.crear(new ClienteRequestDTO("345678-3", "Lácteos del Sur", null, null, null, true));

        List<ClienteDTO> filtroNombre = clienteService.listar("Este");
        assertEquals(1, filtroNombre.size());
        assertEquals("Helados del Este", filtroNombre.get(0).razonSocial());

        List<ClienteDTO> filtroRuc = clienteService.listar("789101");
        assertEquals(1, filtroRuc.size());
        assertEquals("Supermercado Central", filtroRuc.get(0).razonSocial());

        List<ClienteDTO> todos = clienteService.listar(null);
        assertEquals(3, todos.size());
    }

    @Test
    @DisplayName("obtenerOCrearClienteDesdeVenta crea cliente nuevo si no existe")
    void testObtenerOCrearClienteDesdeVentaNuevo() {
        Cliente c = clienteService.obtenerOCrearClienteDesdeVenta("5555555-5", "Cliente Nuevo Venta", "Avda Brasil 456");

        assertNotNull(c);
        assertNotNull(c.getId());
        assertEquals("5555555-5", c.getRuc());
        assertEquals("Cliente Nuevo Venta", c.getRazonSocial());
        assertEquals("Avda Brasil 456", c.getDireccion());
        assertTrue(c.getActivo());
    }

    @Test
    @DisplayName("obtenerOCrearClienteDesdeVenta actualiza datos si el cliente ya existe y difieren")
    void testObtenerOCrearClienteDesdeVentaExistente() {
        clienteService.crear(new ClienteRequestDTO("6666666-6", "Nombre Viejo", "Direccion Vieja", null, null, true));

        Cliente c = clienteService.obtenerOCrearClienteDesdeVenta("6666666-6", "Nombre Actualizado", "Direccion Nueva");

        assertEquals("Nombre Actualizado", c.getRazonSocial());
        assertEquals("Direccion Nueva", c.getDireccion());
        assertNotNull(c.getFechaActualizacion());
    }
}
