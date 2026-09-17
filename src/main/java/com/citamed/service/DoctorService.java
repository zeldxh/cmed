package com.citamed.service;

import com.citamed.domain.Doctor;
import com.citamed.domain.Especialidad;
import com.citamed.domain.HorarioBloqueado;
import com.citamed.domain.Horario;
import com.citamed.domain.DiaSemana;
import com.citamed.domain.Rol;
import com.citamed.repository.DoctorRepository;
import com.citamed.repository.HorarioBloqueadoRepository;
import com.citamed.repository.HorarioRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {

    // Operaciones de médicos, horarios semanales y bloqueos de agenda.
    private final DoctorRepository doctorRepository;
    private final HorarioBloqueadoRepository horarioBloqueadoRepository;
    private final HorarioRepository horarioRepository;
    private final UsuarioService usuarioService;

    public DoctorService(DoctorRepository doctorRepository,
            HorarioBloqueadoRepository horarioBloqueadoRepository,
            HorarioRepository horarioRepository, UsuarioService usuarioService) {
        this.doctorRepository = doctorRepository;
        this.horarioBloqueadoRepository = horarioBloqueadoRepository;
        this.horarioRepository = horarioRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctores(boolean activo) {
        if (activo) {
            return doctorRepository.findByActivoTrue();
        }
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctoresPorEspecialidad(Especialidad especialidad) {
        return doctorRepository.findByEspecialidadAndActivoTrue(especialidad);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctor(Integer idDoctor) {
        return doctorRepository.findById(idDoctor);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorPorUsuario(Integer idUsuario) {
        return doctorRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<HorarioBloqueado> getBloqueos(Doctor doctor) {
        return horarioBloqueadoRepository.findByDoctorOrderByFechaInicioAsc(doctor);
    }

    @Transactional
    public Doctor save(Doctor doctor) {
        // Primero se guarda el usuario y después se relaciona con el doctor.
        doctor.getUsuario().setRol(Rol.DOCTOR);
        doctor.getUsuario().setActivo(doctor.isActivo());
        var usuario = usuarioService.save(doctor.getUsuario());
        doctor.setUsuario(usuario);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor guardarConHorario(Doctor doctor, DiaSemana diaSemana,
            LocalTime horaInicio, LocalTime horaFin) {
        // El médico y su primer horario se registran en una misma transacción.
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalStateException("La hora final debe ser posterior a la hora inicial.");
        }
        var doctorGuardado = save(doctor);
        var horario = new Horario();
        horario.setDoctor(doctorGuardado);
        horario.setDiaSemana(diaSemana);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horarioRepository.save(horario);
        return doctorGuardado;
    }

    @Transactional(readOnly = true)
    public List<Horario> getHorarios(Doctor doctor) {
        return horarioRepository.findByDoctor(doctor);
    }

    @Transactional(readOnly = true)
    public List<Horario> getHorarios(Doctor doctor, DiaSemana diaSemana) {
        return horarioRepository.findByDoctorAndDiaSemana(doctor, diaSemana);
    }

    @Transactional
    public void cambiarEstado(Integer idDoctor) {
        // El estado se mantiene igual en el doctor y en su usuario.
        var doctorOpt = doctorRepository.findById(idDoctor);
        if (doctorOpt.isPresent()) {
            var doctor = doctorOpt.get();
            doctor.setActivo(!doctor.isActivo());
            doctor.getUsuario().setActivo(doctor.isActivo());
            usuarioService.save(doctor.getUsuario());
            doctorRepository.save(doctor);
        }
    }

    @Transactional
    public HorarioBloqueado guardarBloqueo(HorarioBloqueado bloqueo) {
        return horarioBloqueadoRepository.save(bloqueo);
    }
}
