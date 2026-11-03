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

    @org.springframework.beans.factory.annotation.Autowired
    private SesionAuthService sesionAuthService;

    @org.springframework.beans.factory.annotation.Autowired
    private AuditoriaService auditoriaService;

    @org.springframework.beans.factory.annotation.Autowired
    private LimitadorIntentos limitadorIntentos;

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
            exigirAdministrativoSiCorresponde(null, rol);
            usuario.setRol(rol);
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    private static boolean esRolAdministrativo(Rol rol) {
        return rol != null && rol.getNombreRol() != null
                && ("ADMIN".equalsIgnoreCase(rol.getNombreRol()) || "OWNER".equalsIgnoreCase(rol.getNombreRol()));
    }

    private static boolean llamadorEsAdministrativo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN")
                        || a.getAuthority().equals("ROLE_OWNER") || a.getAuthority().equals("OWNER"));
    }

    /**
     * Quien gestiona usuarios por un permiso de página (sin ser ADMIN/OWNER) no puede tocar cuentas administrativas
     * ni otorgar un rol administrativo: sería escalar privilegios (cambiar la clave de un administrador y entrar con ella).
     */
    private static void exigirAdministrativoSiCorresponde(Usuario objetivo, Rol rolNuevo) {
        if (llamadorEsAdministrativo()) {
            return;
        }
        boolean toca = (objetivo != null && (esRolAdministrativo(objetivo.getRol())
                || "admin".equalsIgnoreCase(objetivo.getUsername()) || "owner".equalsIgnoreCase(objetivo.getUsername())))
                || esRolAdministrativo(rolNuevo);
        if (toca) {
            throw new ReglaDeNegocioException("Solo un Administrador o Propietario puede gestionar cuentas administrativas o asignar ese rol.");
        }
    }

    /**
     * Restablece la contraseña de un usuario que la olvidó. No hay correo en el sistema: el administrador recibe una
     * contraseña temporal (una sola vez) y se la entrega al usuario, que está obligado a cambiarla al ingresar.
     * Todas las sesiones abiertas del usuario se cierran.
     */
    @Transactional
    public String restablecerPassword(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && usuario.getUsername().equalsIgnoreCase(auth.getName())) {
            throw new ReglaDeNegocioException("Para cambiar tu propia contraseña usa la opción de cambio de contraseña.");
        }
        boolean llamadorEsOwner = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER") || a.getAuthority().equals("OWNER"));
        String rolDestino = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "";
        boolean destinoPrivilegiado = "OWNER".equalsIgnoreCase(rolDestino) || "ADMIN".equalsIgnoreCase(rolDestino)
                || "owner".equalsIgnoreCase(usuario.getUsername());
        if (destinoPrivilegiado && !llamadorEsOwner) {
            throw new ReglaDeNegocioException("Solo el Propietario (OWNER) puede restablecer la contraseña de un Administrador o del Propietario.");
        }

        String temporal = generarPasswordTemporal();
        usuario.setPasswordHash(passwordEncoder.encode(temporal));
        usuario.setDebeCambiarPassword(true);
        usuarioRepository.save(usuario);
        sesionAuthService.revocarTodas(usuario.getId());
        limitadorIntentos.registrarExito(AuthService.claveLimite(usuario.getUsername())); // por si estaba bloqueado
        auditoriaService.registrar("USUARIOS", "RESTABLECER_PASSWORD", usuario.getId(), usuario.getUsername(),
                "Contraseña restablecida por " + (auth != null ? auth.getName() : "el sistema") + "; se exige cambio al ingresar.");
        return temporal;
    }

    /** 12 caracteres al azar sin símbolos ambiguos (0/O, 1/l/I), fáciles de dictar. */
    public static String generarPasswordTemporal() {
        final String alfabeto = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(alfabeto.charAt(random.nextInt(alfabeto.length())));
        }
        return sb.toString();
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        exigirAdministrativoSiCorresponde(usuario, null);

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

        boolean renombrado = !usuario.getUsername().equals(dto.getUsername());
        usuario.setUsername(dto.getUsername());
        boolean cambioClave = dto.getPassword() != null && !dto.getPassword().isBlank();
        if (cambioClave) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }
        // Nueva clave, usuario desactivado o renombrado: las sesiones abiertas dejan de servir
        if (cambioClave || renombrado || Boolean.FALSE.equals(dto.getEstado())) {
            sesionAuthService.revocarTodas(usuario.getId());
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
            exigirAdministrativoSiCorresponde(null, rol);
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
        exigirAdministrativoSiCorresponde(usuario, null);

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
