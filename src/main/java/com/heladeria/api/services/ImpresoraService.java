package com.heladeria.api.services;

import com.heladeria.api.dto.ImpresoraDTO;
import com.heladeria.api.dto.ImpresoraRequestDTO;
import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.TipoConexion;
import com.heladeria.api.exceptions.ConflictException;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.repositories.TicketeraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ImpresoraService {

    private static final int PUERTO_DEFAULT_RED = 9100;
    private static final int PUERTO_DEFAULT_CUPS = 631;
    private static final int PUERTO_DEFAULT_WINDOWS = 445;

    private final ImpresoraRepository impresoraRepository;
    private final TicketeraRepository ticketeraRepository;

    public ImpresoraService(ImpresoraRepository impresoraRepository, TicketeraRepository ticketeraRepository) {
        this.impresoraRepository = impresoraRepository;
        this.ticketeraRepository = ticketeraRepository;
    }

    @Transactional(readOnly = true)
    public List<ImpresoraDTO> findAll() {
        return impresoraRepository.findAll().stream().map(ImpresoraDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ImpresoraDTO findById(Long id) {
        return ImpresoraDTO.from(getOrThrow(id));
    }

    @Transactional
    public ImpresoraDTO create(ImpresoraRequestDTO request) {
        if (!StringUtils.hasText(request.getNombre())) {
            throw new IllegalArgumentException("nombre es requerido");
        }
        if (!StringUtils.hasText(request.getIp())) {
            throw new IllegalArgumentException("ip es requerida");
        }
        if (impresoraRepository.existsByNombre(request.getNombre().trim())) {
            throw new ConflictException("Ya existe una impresora con el nombre: " + request.getNombre().trim());
        }

        Impresora impresora = new Impresora();
        aplicar(impresora, request);
        return ImpresoraDTO.from(impresoraRepository.save(impresora));
    }

    @Transactional
    public ImpresoraDTO update(Long id, ImpresoraRequestDTO request) {
        if (!StringUtils.hasText(request.getNombre())) {
            throw new IllegalArgumentException("nombre es requerido");
        }
        if (!StringUtils.hasText(request.getIp())) {
            throw new IllegalArgumentException("ip es requerida");
        }
        Impresora impresora = getOrThrow(id);
        if (impresoraRepository.existsByNombreAndIdNot(request.getNombre().trim(), id)) {
            throw new ConflictException("Ya existe una impresora con el nombre: " + request.getNombre().trim());
        }

        aplicar(impresora, request);
        return ImpresoraDTO.from(impresoraRepository.save(impresora));
    }

    @Transactional
    public void delete(Long id) {
        Impresora impresora = getOrThrow(id);
        if (ticketeraRepository.existsByImpresoraId(id)) {
            throw new ConflictException("No se puede eliminar la impresora porque está asignada a una ticketera");
        }
        impresoraRepository.delete(impresora);
    }

    public Impresora getOrThrow(Long id) {
        return impresoraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Impresora no encontrada"));
    }

    private void aplicar(Impresora impresora, ImpresoraRequestDTO request) {
        TipoConexion tipoConexion = StringUtils.hasText(request.getTipoConexion())
                ? parseTipoConexion(request.getTipoConexion())
                : TipoConexion.RED;

        if (tipoConexion == TipoConexion.CUPS && !StringUtils.hasText(request.getCola())) {
            throw new IllegalArgumentException("cola es requerida para conexión CUPS");
        }
        if (tipoConexion == TipoConexion.WINDOWS && !StringUtils.hasText(request.getCola())) {
            throw new IllegalArgumentException("cola (recurso compartido) es requerida para conexión WINDOWS");
        }

        Integer puerto = request.getPuerto() != null
                ? request.getPuerto()
                : (tipoConexion == TipoConexion.RED ? PUERTO_DEFAULT_RED
                        : (tipoConexion == TipoConexion.WINDOWS ? PUERTO_DEFAULT_WINDOWS : PUERTO_DEFAULT_CUPS));

        String ipLimpia = request.getIp().trim().replaceFirst("^[\\\\/]+", "");
        String colaLimpia = (tipoConexion == TipoConexion.CUPS || tipoConexion == TipoConexion.WINDOWS) && StringUtils.hasText(request.getCola())
                ? request.getCola().trim().replaceFirst("^[\\\\/]+", "")
                : null;

        impresora.setNombre(request.getNombre().trim());
        impresora.setTipoConexion(tipoConexion);
        impresora.setIp(ipLimpia);
        impresora.setPuerto(puerto);
        impresora.setCola(colaLimpia);

        if (tipoConexion == TipoConexion.WINDOWS) {
            impresora.setUsuario(StringUtils.hasText(request.getUsuario()) ? request.getUsuario().trim() : null);
            if (request.getPassword() != null) {
                impresora.setPassword(StringUtils.hasText(request.getPassword()) ? request.getPassword().trim() : null);
            }
        } else {
            impresora.setUsuario(null);
            impresora.setPassword(null);
        }
    }

    private TipoConexion parseTipoConexion(String valor) {
        try {
            return TipoConexion.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipoConexion debe ser RED, CUPS o WINDOWS");
        }
    }
}
