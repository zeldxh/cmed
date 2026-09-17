package com.citamed.service;

import com.citamed.domain.Paciente;
import com.citamed.domain.Rol;
import com.citamed.repository.PacienteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    // Operaciones de registro, consulta y mantenimiento de pacientes.
    private final PacienteRepository pacienteRepository;
    private final UsuarioService usuarioService;

    public PacienteService(PacienteRepository pacienteRepository, UsuarioService usuarioService) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<Paciente> getPacientes(boolean activo) {
        if (activo) {
            return pacienteRepository.findByUsuarioActivoTrue();
        }
        return pacienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Paciente> buscarPacientes(String busqueda) {
        // La misma entrada permite buscar por nombre o cédula.
        if (busqueda == null || busqueda.isBlank()) {
            return pacienteRepository.findAll();
        }
        return pacienteRepository.findByUsuarioNombreContainingIgnoreCaseOrCedulaContainingIgnoreCase(
                busqueda, busqueda);
    }

    @Transactional(readOnly = true)
    public Optional<Paciente> getPaciente(Integer idPaciente) {
        return pacienteRepository.findById(idPaciente);
    }

    @Transactional(readOnly = true)
    public Optional<Paciente> getPacientePorUsuario(Integer idUsuario) {
        return pacienteRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional
    public Paciente registrarPaciente(Paciente paciente) {
        // Todo registro público se crea con el rol de paciente.
        paciente.getUsuario().setRol(Rol.PACIENTE);
        paciente.getUsuario().setActivo(true);
        var usuario = usuarioService.save(paciente.getUsuario());
        paciente.setUsuario(usuario);
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public Paciente save(Paciente paciente) {
        if (paciente.getIdPaciente() == null) {
            return registrarPaciente(paciente);
        }
        var pacienteGuardado = pacienteRepository.findById(paciente.getIdPaciente())
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe."));
        pacienteGuardado.getUsuario().setNombre(paciente.getUsuario().getNombre());
        pacienteGuardado.getUsuario().setCorreo(paciente.getUsuario().getCorreo());
        pacienteGuardado.setCedula(paciente.getCedula());
        pacienteGuardado.setTelefono(paciente.getTelefono());
        pacienteGuardado.setFechaNacimiento(paciente.getFechaNacimiento());
        pacienteGuardado.setDireccion(paciente.getDireccion());
        usuarioService.save(pacienteGuardado.getUsuario());
        return pacienteRepository.save(pacienteGuardado);
    }

    @Transactional
    public void cambiarEstado(Integer idPaciente) {
        // Desactivar al paciente también impide que pueda iniciar sesión.
        var pacienteOpt = pacienteRepository.findById(idPaciente);
        if (pacienteOpt.isPresent()) {
            var paciente = pacienteOpt.get();
            paciente.getUsuario().setActivo(!paciente.getUsuario().isActivo());
            usuarioService.save(paciente.getUsuario());
            pacienteRepository.save(paciente);
        }
    }
}
