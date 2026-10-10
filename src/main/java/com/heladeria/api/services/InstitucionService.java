package com.heladeria.api.services;

import com.heladeria.api.dto.InstitucionDTO;
import com.heladeria.api.entities.Institucion;
import com.heladeria.api.repositories.InstitucionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Base64;

@Service
public class InstitucionService {

    private static final Long SINGLETON_ID = 1L;

    private final InstitucionRepository institucionRepository;

    public InstitucionService(InstitucionRepository institucionRepository) {
        this.institucionRepository = institucionRepository;
    }

    @Transactional
    public InstitucionDTO get() {
        return InstitucionDTO.from(obtenerOCrear());
    }

    @Transactional
    public InstitucionDTO update(InstitucionDTO request) {
        if (StringUtils.hasText(request.getLogoBase64())) {
            try {
                Base64.getDecoder().decode(request.getLogoBase64());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("logoBase64 no es un base64 válido");
            }
        }

        Institucion institucion = obtenerOCrear();
        institucion.setNombre(request.getNombre() != null ? request.getNombre() : "");
        institucion.setLogoBase64(request.getLogoBase64());

        if (request.getRuc() != null) institucion.setRuc(request.getRuc());
        if (request.getTimbrado() != null) institucion.setTimbrado(request.getTimbrado());
        if (request.getTimbradoVencimiento() != null) institucion.setTimbradoVencimiento(request.getTimbradoVencimiento());
        if (request.getEstablecimiento() != null) institucion.setEstablecimiento(request.getEstablecimiento());
        if (request.getPuntoEmision() != null) institucion.setPuntoEmision(request.getPuntoEmision());
        if (request.getUltimoNumeroFactura() != null) institucion.setUltimoNumeroFactura(request.getUltimoNumeroFactura());
        if (request.getDireccion() != null) institucion.setDireccion(request.getDireccion());
        if (request.getTelefono() != null) institucion.setTelefono(request.getTelefono());
        if (request.getCiudad() != null) institucion.setCiudad(request.getCiudad());

        return InstitucionDTO.from(institucionRepository.save(institucion));
    }

    public Institucion obtenerOCrear() {
        return institucionRepository.findById(SINGLETON_ID).orElseGet(() -> {
            Institucion institucion = new Institucion();
            institucion.setId(SINGLETON_ID);
            institucion.setNombre("Heladería");
            return institucionRepository.save(institucion);
        });
    }
}
