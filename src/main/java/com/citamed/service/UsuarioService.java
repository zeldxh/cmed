package com.citamed.service;

import com.citamed.domain.Rol;
import com.citamed.domain.Usuario;
import com.citamed.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    // Reglas relacionadas con cuentas, acceso y contraseñas.
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        // Permite listar todos los usuarios o solamente los activos.
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuariosPorRol(Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuario.getIdUsuario() == null && usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalStateException("El correo electrónico ya está registrado.");
        }
        if (usuario.getIdUsuario() == null) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public boolean verificarContrasena(Integer idUsuario, String contrasena) {
        return usuarioRepository.findById(idUsuario)
                .map(usuario -> passwordEncoder.matches(contrasena, usuario.getContrasena()))
                .orElse(false);
    }

    @Transactional
    public boolean cambiarContrasena(Integer idUsuario, String actual, String nueva, String confirmar) {
        // Se valida la contraseña actual y la confirmación antes de guardar.
        var usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        var usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(actual, usuario.getContrasena()) || !nueva.equals(confirmar)) {
            return false;
        }
        usuario.setContrasena(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
        return true;
    }
}
