package com.heladeria.api.services;

import com.heladeria.api.dto.UsuarioDTO;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public UsuarioDTO guardar(UsuarioDTO dto) {
        if (usuarioRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + dto.getUsername());
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es requerida para crear un nuevo usuario");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        usuario.setPasswordNuncaExpira(dto.getPasswordNuncaExpira() != null ? dto.getPasswordNuncaExpira() : false);
        usuario.setDebeCambiarPassword(dto.getDebeCambiarPassword() != null ? dto.getDebeCambiarPassword() : false);
        usuario.setPasswordExpiraEl(dto.getPasswordExpiraEl());

        if (dto.getRolId() != null) {
            Rol rol = rolRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + dto.getRolId()));
            usuario.setRol(rol);
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth != null ? auth.getName() : null;
        boolean isCallerOwner = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER") || a.getAuthority().equals("OWNER"));
        boolean esMismoUsuario = currentUsername != null && currentUsername.equalsIgnoreCase(usuario.getUsername());
        boolean targetEsOwner = "owner".equalsIgnoreCase(usuario.getUsername()) ||
                (usuario.getRol() != null && "OWNER".equalsIgnoreCase(usuario.getRol().getNombreRol()));

        // Solo un OWNER o el mismo usuario gestionando su cuenta puede modificar a un usuario con rol OWNER
        if (targetEsOwner && !isCallerOwner && !esMismoUsuario) {
            throw new ReglaDeNegocioException("Un usuario Administrador no tiene autorización para modificar los datos, rol ni estado del Propietario (OWNER).");
        }

        // El usuario 'owner' principal no puede ser deshabilitado
        if ("owner".equalsIgnoreCase(usuario.getUsername()) && Boolean.FALSE.equals(dto.getEstado())) {
            throw new ReglaDeNegocioException("La cuenta del Propietario principal 'owner' no puede ser deshabilitada.");
        }

        // El usuario 'admin' principal no puede ser deshabilitado
        if ("admin".equalsIgnoreCase(usuario.getUsername()) && Boolean.FALSE.equals(dto.getEstado())) {
            throw new ReglaDeNegocioException("La cuenta principal 'admin' no puede ser deshabilitada.");
        }

        // Prohibir renombrar 'owner' o 'admin'
        if ("owner".equalsIgnoreCase(usuario.getUsername()) && !dto.getUsername().equalsIgnoreCase("owner")) {
            throw new ReglaDeNegocioException("El nombre del usuario principal 'owner' no puede ser modificado.");
        }
        if ("admin".equalsIgnoreCase(usuario.getUsername()) && !dto.getUsername().equalsIgnoreCase("admin")) {
            throw new ReglaDeNegocioException("El nombre del usuario principal 'admin' no puede ser modificado.");
        }

        if (!usuario.getUsername().equalsIgnoreCase(dto.getUsername()) &&
                usuarioRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + dto.getUsername());
        }

        usuario.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }
        if (dto.getPasswordNuncaExpira() != null) {
            usuario.setPasswordNuncaExpira(dto.getPasswordNuncaExpira());
        }
        if (dto.getDebeCambiarPassword() != null) {
            usuario.setDebeCambiarPassword(dto.getDebeCambiarPassword());
        }
        usuario.setPasswordExpiraEl(dto.getPasswordExpiraEl());

        if (dto.getRolId() != null) {
            Rol rol = rolRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + dto.getRolId()));
            if ("owner".equalsIgnoreCase(usuario.getUsername()) && !"OWNER".equalsIgnoreCase(rol.getNombreRol())) {
                throw new ReglaDeNegocioException("El usuario 'owner' debe mantener siempre el rol OWNER.");
            }
            if ("admin".equalsIgnoreCase(usuario.getUsername()) && !"ADMIN".equalsIgnoreCase(rol.getNombreRol()) && !"OWNER".equalsIgnoreCase(rol.getNombreRol())) {
                throw new ReglaDeNegocioException("El usuario 'admin' debe mantener el rol ADMIN u OWNER.");
            }
            usuario.setRol(rol);
        } else {
            if ("owner".equalsIgnoreCase(usuario.getUsername()) || "admin".equalsIgnoreCase(usuario.getUsername())) {
                throw new ReglaDeNegocioException("No se puede desasignar el rol de un usuario principal del sistema.");
            }
            usuario.setRol(null);
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        // 1. Prohibir eliminación de cuentas protegidas del sistema
        if ("owner".equalsIgnoreCase(usuario.getUsername())) {
            throw new ReglaDeNegocioException("El usuario principal 'owner' (Propietario) está protegido y no puede ser eliminado.");
        }
        if ("admin".equalsIgnoreCase(usuario.getUsername())) {
            throw new ReglaDeNegocioException("El usuario principal 'admin' está protegido y no puede ser eliminado.");
        }
        if (usuario.getRol() != null && "OWNER".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ReglaDeNegocioException("Los usuarios con rol OWNER (Propietario) no pueden ser eliminados.");
        }

        // 2. Prohibir auto-eliminación
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && usuario.getUsername().equalsIgnoreCase(auth.getName())) {
            throw new ReglaDeNegocioException("No puedes eliminar tu propia cuenta de usuario.");
        }

        // 3. Un usuario con rol ADMIN (no OWNER) no puede eliminar a un ADMIN ni a un OWNER
        boolean isCallerOwner = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER") || a.getAuthority().equals("OWNER"));
        if (!isCallerOwner && usuario.getRol() != null && "ADMIN".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ReglaDeNegocioException("Un usuario Administrador no tiene autorización para eliminar a otro Administrador.");
        }

        usuarioRepository.delete(usuario);
    }

    public UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEstado(u.getEstado());
        dto.setPasswordNuncaExpira(u.getPasswordNuncaExpira());
        dto.setDebeCambiarPassword(u.getDebeCambiarPassword());
        dto.setPasswordExpiraEl(u.getPasswordExpiraEl());
        if (u.getRol() != null) {
            dto.setRolId(u.getRol().getId());
            dto.setRolNombre(u.getRol().getNombreRol());
        }
        return dto;
    }
}
