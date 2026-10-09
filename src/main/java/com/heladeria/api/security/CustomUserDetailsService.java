package com.heladeria.api.security;

import com.heladeria.api.entities.Pagina;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "SIN_ROL";

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rolNombre));

        // Cada pagina asignada al rol se convierte en una authority propia (ej "PAGINA_sabores")
        if (usuario.getRol() != null && usuario.getRol().getPaginas() != null) {
            for (Pagina pagina : usuario.getRol().getPaginas()) {
                authorities.add(new SimpleGrantedAuthority("PAGINA_" + pagina.getClave()));
            }
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(authorities)
                .disabled(!Boolean.TRUE.equals(usuario.getEstado()))
                .build();
    }
}
