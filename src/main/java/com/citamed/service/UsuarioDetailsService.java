package com.citamed.service;

import com.citamed.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService")
public class UsuarioDetailsService implements UserDetailsService {

    // Convierte al Usuario de CitaMed en el UserDetails que usa Spring Security.
    private final UsuarioRepository usuarioRepository;
    private final HttpSession session;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository, HttpSession session) {
        this.usuarioRepository = usuarioRepository;
        this.session = session;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByCorreo(correo)
                .filter(u -> u.isActivo())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        // Se guardan los datos del usuario en sesion para reutilizarlos en las pantallas.
        session.setAttribute("idUsuario", usuario.getIdUsuario());
        session.setAttribute("rol", usuario.getRol().name());
        session.setAttribute("nombreUsuario", usuario.getNombre());

        Set<SimpleGrantedAuthority> autoridades = Set.of(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return new User(usuario.getCorreo(), usuario.getContrasena(), autoridades);
    }
}
