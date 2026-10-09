package com.heladeria.api.services;

import com.heladeria.api.dto.RolDTO;
import com.heladeria.api.entities.Pagina;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.PaginaRepository;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PaginaRepository paginaRepository;
    private final LicenciaService licenciaService;

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      PaginaRepository paginaRepository,
                      LicenciaService licenciaService) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.paginaRepository = paginaRepository;
        this.licenciaService = licenciaService;
    }

    @Transactional(readOnly = true)
    public List<RolDTO> listarTodos() {
        return rolRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RolDTO obtenerPorId(Long id) {
        return rolRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + id));
    }

    @Transactional
    public RolDTO guardar(RolDTO dto) {
        if (rolRepository.existsByNombreRolIgnoreCase(dto.getNombre())) {
            throw new IllegalStateException("Ya existe un rol con el nombre: " + dto.getNombre());
        }
        Rol rol = new Rol();
        aplicarCampos(rol, dto);
        return toDTO(rolRepository.save(rol));
    }

    @Transactional
    public RolDTO actualizar(Long id, RolDTO dto) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + id));

        if (!rol.getNombreRol().equalsIgnoreCase(dto.getNombre()) &&
                rolRepository.existsByNombreRolIgnoreCase(dto.getNombre())) {
            throw new IllegalStateException("Ya existe un rol con el nombre: " + dto.getNombre());
        }

        aplicarCampos(rol, dto);
        return toDTO(rolRepository.save(rol));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Rol no encontrado con ID: " + id);
        }
        if (usuarioRepository.existsByRol_Id(id)) {
            throw new IllegalStateException("No se puede eliminar el rol: hay usuarios asignados a él");
        }
        rolRepository.deleteById(id);
    }

    private void aplicarCampos(Rol rol, RolDTO dto) {
        rol.setNombreRol(dto.getNombre());
        rol.setPaginas(resolverPaginasLicenciadas(dto.getPaginas()));
    }

    public Set<Pagina> resolverPaginasLicenciadas(Set<String> claves) {
        if (claves == null || claves.isEmpty()) {
            return new HashSet<>();
        }

        Map<String, Pagina> licenciadasPorClave = licenciaService.obtenerPaginasLicenciadas().stream()
                .collect(Collectors.toMap(Pagina::getClave, Function.identity()));

        Set<Pagina> resultado = new HashSet<>();
        for (String clave : claves) {
            Pagina licenciada = licenciadasPorClave.get(clave);
            if (licenciada == null) {
                if (paginaRepository.findByClave(clave).isEmpty()) {
                    throw new IllegalStateException("Pagina desconocida: " + clave);
                }
                throw new IllegalStateException("La pagina '" + clave + "' no esta incluida en el plan de licencia actual");
            }
            resultado.add(licenciada);
        }
        return resultado;
    }

    public RolDTO toDTO(Rol r) {
        RolDTO dto = new RolDTO();
        dto.setId(r.getId());
        dto.setNombre(r.getNombreRol());
        if (r.getPaginas() != null) {
            dto.setPaginas(r.getPaginas().stream()
                    .map(Pagina::getClave)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
