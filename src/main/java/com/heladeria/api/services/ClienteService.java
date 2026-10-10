package com.heladeria.api.services;

import com.heladeria.api.dto.ClienteDTO;
import com.heladeria.api.dto.ClienteRequestDTO;
import com.heladeria.api.entities.Cliente;
import com.heladeria.api.exceptions.ConflictException;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteDTO> listar(String filtro) {
        List<Cliente> clientes;
        if (filtro != null && !filtro.trim().isEmpty()) {
            clientes = clienteRepository.buscarPorFiltro(filtro.trim());
        } else {
            clientes = clienteRepository.findAllByOrderByRazonSocialAsc();
        }
        return clientes.stream().map(this::toDTO).toList();
    }

    public Optional<ClienteDTO> buscarPorRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) return Optional.empty();
        return clienteRepository.findByRucIgnoreCase(ruc.trim()).map(this::toDTO);
    }

    public ClienteDTO obtenerPorId(Long id) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente con ID " + id + " no encontrado."));
        return toDTO(c);
    }

    @Transactional
    public ClienteDTO crear(ClienteRequestDTO dto) {
        String rucLimpio = dto.getRuc().trim();
        if (clienteRepository.existsByRucIgnoreCase(rucLimpio)) {
            throw new ConflictException("Ya existe un cliente registrado con el RUC/C.I. " + rucLimpio);
        }
        Cliente nuevo = new Cliente();
        nuevo.setRuc(rucLimpio);
        nuevo.setRazonSocial(dto.getRazonSocial().trim());
        nuevo.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        nuevo.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        nuevo.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        nuevo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        nuevo.setFechaCreacion(LocalDateTime.now());
        return toDTO(clienteRepository.save(nuevo));
    }

    @Transactional
    public ClienteDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente con ID " + id + " no encontrado."));

        String rucLimpio = dto.getRuc().trim();
        if (!existente.getRuc().equalsIgnoreCase(rucLimpio) && clienteRepository.existsByRucIgnoreCase(rucLimpio)) {
            throw new ConflictException("Ya existe otro cliente con el RUC/C.I. " + rucLimpio);
        }

        existente.setRuc(rucLimpio);
        existente.setRazonSocial(dto.getRazonSocial().trim());
        existente.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        existente.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        existente.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        if (dto.getActivo() != null) existente.setActivo(dto.getActivo());
        existente.setFechaActualizacion(LocalDateTime.now());
        return toDTO(clienteRepository.save(existente));
    }

    @Transactional
    public ClienteDTO cambiarEstado(Long id, boolean activo) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente con ID " + id + " no encontrado."));
        c.setActivo(activo);
        c.setFechaActualizacion(LocalDateTime.now());
        return toDTO(clienteRepository.save(c));
    }

    /**
     * Utilizado transparentemente por PedidoService al registrar ventas en el POS.
     */
    @Transactional
    public Cliente obtenerOCrearClienteDesdeVenta(String ruc, String razonSocial, String direccion) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return null;
        }
        String rucLimpio = ruc.trim();
        return clienteRepository.findByRucIgnoreCase(rucLimpio)
                .map(existente -> {
                    boolean modificado = false;
                    if (razonSocial != null && !razonSocial.isBlank() && !razonSocial.equalsIgnoreCase(existente.getRazonSocial())) {
                        existente.setRazonSocial(razonSocial.trim());
                        modificado = true;
                    }
                    if (direccion != null && !direccion.isBlank() && !direccion.equalsIgnoreCase(existente.getDireccion())) {
                        existente.setDireccion(direccion.trim());
                        modificado = true;
                    }
                    if (modificado) {
                        existente.setFechaActualizacion(LocalDateTime.now());
                        return clienteRepository.save(existente);
                    }
                    return existente;
                })
                .orElseGet(() -> {
                    Cliente nuevo = new Cliente();
                    nuevo.setRuc(rucLimpio);
                    nuevo.setRazonSocial((razonSocial != null && !razonSocial.isBlank()) ? razonSocial.trim() : "Cliente Ocasional");
                    nuevo.setDireccion((direccion != null && !direccion.isBlank()) ? direccion.trim() : null);
                    nuevo.setActivo(true);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    return clienteRepository.save(nuevo);
                });
    }

    private ClienteDTO toDTO(Cliente c) {
        return new ClienteDTO(
                c.getId(),
                c.getRuc(),
                c.getRazonSocial(),
                c.getDireccion(),
                c.getTelefono(),
                c.getEmail(),
                c.getActivo(),
                c.getFechaCreacion(),
                c.getFechaActualizacion()
        );
    }
}
