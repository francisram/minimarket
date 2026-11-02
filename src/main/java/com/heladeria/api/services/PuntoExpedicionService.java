package com.heladeria.api.services;

import com.heladeria.api.dto.PuntoExpedicionDTO;
import com.heladeria.api.dto.PuntoExpedicionRequestDTO;
import com.heladeria.api.entities.Institucion;
import com.heladeria.api.entities.PuntoExpedicion;
import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.InstitucionRepository;
import com.heladeria.api.repositories.PuntoExpedicionRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PuntoExpedicionService {

    private final PuntoExpedicionRepository puntoExpedicionRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final InstitucionRepository institucionRepository;
    private final AuditoriaService auditoriaService;

    public PuntoExpedicionService(PuntoExpedicionRepository puntoExpedicionRepository,
                                  SesionCajaRepository sesionCajaRepository,
                                  InstitucionRepository institucionRepository,
                                  AuditoriaService auditoriaService) {
        this.puntoExpedicionRepository = puntoExpedicionRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.institucionRepository = institucionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<PuntoExpedicionDTO> listarTodos() {
        List<PuntoExpedicion> puntos = puntoExpedicionRepository.findAllByOrderByEstablecimientoAscPuntoEmisionAsc();
        return puntos.stream().map(this::convertirADTOConEstado).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PuntoExpedicionDTO> listarDisponibles() {
        List<PuntoExpedicion> puntos = puntoExpedicionRepository.findByActivoTrueOrderByEstablecimientoAscPuntoEmisionAsc();
        return puntos.stream()
                .filter(p -> !sesionCajaRepository.existsByPuntoExpedicion_IdAndEstado(p.getId(), EstadoSesionCaja.ABIERTA))
                .map(p -> PuntoExpedicionDTO.fromEntity(p, false, null, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PuntoExpedicionDTO obtenerPorId(Long id) {
        PuntoExpedicion punto = puntoExpedicionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de expedición no encontrado con id " + id));
        return convertirADTOConEstado(punto);
    }

    @Transactional
    public PuntoExpedicionDTO crear(PuntoExpedicionRequestDTO req, String username) {
        String estab = normalizarCodigo(req.getEstablecimiento());
        String punto = normalizarCodigo(req.getPuntoEmision());

        if (puntoExpedicionRepository.existsByEstablecimientoAndPuntoEmision(estab, punto)) {
            throw new ReglaDeNegocioException("Ya existe un punto de expedición configurado para el establecimiento " + estab + " y punto de emisión " + punto + ".");
        }

        if (req.getNumeroInicioFactura() != null && req.getNumeroFinFactura() != null && req.getNumeroInicioFactura() > req.getNumeroFinFactura()) {
            throw new ReglaDeNegocioException("El número de inicio (" + req.getNumeroInicioFactura() + ") no puede ser mayor que el número fin (" + req.getNumeroFinFactura() + ").");
        }

        String timbrado = req.getTimbrado();
        LocalDate timbradoInicio = req.getTimbradoInicio();
        LocalDate timbradoVencimiento = req.getTimbradoVencimiento();

        if (timbrado == null || timbrado.isBlank()) {
            Institucion inst = institucionRepository.findById(1L).orElse(null);
            if (inst != null && inst.getTimbrado() != null && !inst.getTimbrado().isBlank()) {
                timbrado = inst.getTimbrado();
                timbradoInicio = inst.getTimbradoInicio();
                timbradoVencimiento = inst.getTimbradoVencimiento();
            }
        }

        PuntoExpedicion entity = PuntoExpedicion.builder()
                .nombre(req.getNombre().trim())
                .establecimiento(estab)
                .puntoEmision(punto)
                .timbrado(timbrado != null ? timbrado.trim() : null)
                .timbradoInicio(timbradoInicio)
                .timbradoVencimiento(timbradoVencimiento)
                .numeroInicioFactura(req.getNumeroInicioFactura() != null ? req.getNumeroInicioFactura() : 1L)
                .numeroFinFactura(req.getNumeroFinFactura() != null ? req.getNumeroFinFactura() : 1000L)
                .ultimoNumeroFactura(req.getUltimoNumeroFactura() != null ? req.getUltimoNumeroFactura() : 0L)
                .activo(req.getActivo() != null ? req.getActivo() : true)
                .build();

        PuntoExpedicion guardado = puntoExpedicionRepository.save(entity);

        auditoriaService.registrarLog(
                username,
                "FACTURACION",
                "CREAR_PUNTO_EXPEDICION",
                guardado.getId(),
                guardado.getPrefijoLegal() + " - " + guardado.getNombre(),
                "Creación de nuevo punto de expedición fiscal: " + guardado.getPrefijoLegal() + " (Talonario " + guardado.getNumeroInicioFactura() + " al " + guardado.getNumeroFinFactura() + ")",
                null
        );

        return convertirADTOConEstado(guardado);
    }

    @Transactional
    public PuntoExpedicionDTO actualizar(Long id, PuntoExpedicionRequestDTO req, String username) {
        PuntoExpedicion entity = puntoExpedicionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de expedición no encontrado con id " + id));

        String estab = normalizarCodigo(req.getEstablecimiento());
        String punto = normalizarCodigo(req.getPuntoEmision());

        if (puntoExpedicionRepository.existsByEstablecimientoAndPuntoEmisionAndIdNot(estab, punto, id)) {
            throw new ReglaDeNegocioException("Ya existe otro punto de expedición configurado para " + estab + "-" + punto + ".");
        }

        if (req.getNumeroInicioFactura() != null && req.getNumeroFinFactura() != null && req.getNumeroInicioFactura() > req.getNumeroFinFactura()) {
            throw new ReglaDeNegocioException("El número de inicio (" + req.getNumeroInicioFactura() + ") no puede ser mayor que el número fin (" + req.getNumeroFinFactura() + ").");
        }

        entity.setNombre(req.getNombre().trim());
        entity.setEstablecimiento(estab);
        entity.setPuntoEmision(punto);
        if (req.getTimbrado() != null) {
            entity.setTimbrado(req.getTimbrado().trim());
        }
        entity.setTimbradoInicio(req.getTimbradoInicio());
        entity.setTimbradoVencimiento(req.getTimbradoVencimiento());
        if (req.getNumeroInicioFactura() != null) {
            entity.setNumeroInicioFactura(req.getNumeroInicioFactura());
        }
        if (req.getNumeroFinFactura() != null) {
            entity.setNumeroFinFactura(req.getNumeroFinFactura());
        }
        if (req.getUltimoNumeroFactura() != null) {
            entity.setUltimoNumeroFactura(req.getUltimoNumeroFactura());
        }
        if (req.getActivo() != null) {
            if (!req.getActivo() && sesionCajaRepository.existsByPuntoExpedicion_IdAndEstado(id, EstadoSesionCaja.ABIERTA)) {
                throw new ReglaDeNegocioException("No se puede desactivar el punto de expedición porque está actualmente en uso por una caja abierta.");
            }
            entity.setActivo(req.getActivo());
        }

        PuntoExpedicion actualizado = puntoExpedicionRepository.save(entity);

        auditoriaService.registrarLog(
                username,
                "FACTURACION",
                "ACTUALIZAR_PUNTO_EXPEDICION",
                actualizado.getId(),
                actualizado.getPrefijoLegal() + " - " + actualizado.getNombre(),
                "Actualización de datos y talonario de punto de expedición: " + actualizado.getPrefijoLegal(),
                null
        );

        return convertirADTOConEstado(actualizado);
    }

    @Transactional
    public PuntoExpedicionDTO cambiarEstado(Long id, Boolean activo, String username) {
        PuntoExpedicion entity = puntoExpedicionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de expedición no encontrado con id " + id));

        if (Boolean.FALSE.equals(activo) && sesionCajaRepository.existsByPuntoExpedicion_IdAndEstado(id, EstadoSesionCaja.ABIERTA)) {
            throw new ReglaDeNegocioException("No se puede desactivar el punto de expedición '" + entity.getNombre() + "' (" + entity.getPrefijoLegal() + ") porque está actualmente en uso por un turno de caja abierto.");
        }

        entity.setActivo(activo);
        PuntoExpedicion actualizado = puntoExpedicionRepository.save(entity);

        auditoriaService.registrarLog(
                username,
                "FACTURACION",
                "ESTADO_PUNTO_EXPEDICION",
                actualizado.getId(),
                actualizado.getPrefijoLegal() + " - " + actualizado.getNombre(),
                "Cambio de estado activo=" + activo + " para " + actualizado.getPrefijoLegal(),
                null
        );

        return convertirADTOConEstado(actualizado);
    }

    private PuntoExpedicionDTO convertirADTOConEstado(PuntoExpedicion p) {
        Optional<SesionCaja> sesionOpt = sesionCajaRepository.findFirstByPuntoExpedicion_IdAndEstado(p.getId(), EstadoSesionCaja.ABIERTA);
        boolean enUso = sesionOpt.isPresent();
        String usuarioActual = enUso && sesionOpt.get().getUsuarioApertura() != null ? sesionOpt.get().getUsuarioApertura().getUsername() : null;
        Long sesionId = enUso ? sesionOpt.get().getId() : null;
        return PuntoExpedicionDTO.fromEntity(p, enUso, usuarioActual, sesionId);
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "001";
        }
        String clean = codigo.replaceAll("\\D", "");
        if (clean.isBlank()) {
            return "001";
        }
        int val = Integer.parseInt(clean);
        return String.format("%03d", Math.min(val, 999));
    }
}
