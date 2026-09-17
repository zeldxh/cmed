package com.citamed.service;

import com.citamed.domain.Cita;
import com.citamed.domain.Doctor;
import com.citamed.domain.EstadoCita;
import com.citamed.domain.Paciente;
import com.citamed.repository.CitaRepository;
import com.citamed.repository.HorarioBloqueadoRepository;
import com.citamed.repository.HorarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaService {

    // Reglas de reserva, disponibilidad y cambios de estado de las citas.
    // Repositorios necesarios para administrar las citas y sus bloqueos.
    private final CitaRepository citaRepository;
    private final HorarioBloqueadoRepository horarioBloqueadoRepository;
    private final HorarioRepository horarioRepository;
    private final NotificacionService notificacionService;

    public CitaService(CitaRepository citaRepository,
            HorarioBloqueadoRepository horarioBloqueadoRepository,
            HorarioRepository horarioRepository, NotificacionService notificacionService) {
        this.citaRepository = citaRepository;
        this.horarioBloqueadoRepository = horarioBloqueadoRepository;
        this.horarioRepository = horarioRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public List<Cita> getCitas() {
        return citaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cita> getCita(Integer idCita) {
        return citaRepository.findById(idCita);
    }

    @Transactional(readOnly = true)
    public List<Cita> getCitasPorPaciente(Paciente paciente) {
        return citaRepository.findByPacienteOrderByFechaDescHoraDesc(paciente);
    }

    @Transactional(readOnly = true)
    public Optional<Cita> getProximaCita(Paciente paciente) {
        Cita proximaCita = null;
        LocalDateTime ahora = LocalDateTime.now();
        for (var cita : citaRepository.findByPacienteOrderByFechaDescHoraDesc(paciente)) {
            LocalDateTime fechaHora = LocalDateTime.of(cita.getFecha(), cita.getHora());
            boolean activa = cita.getEstado() == EstadoCita.PENDIENTE
                    || cita.getEstado() == EstadoCita.CONFIRMADA;
            if (activa && fechaHora.isAfter(ahora)) {
                if (proximaCita == null) {
                    proximaCita = cita;
                } else {
                    LocalDateTime fechaProxima = LocalDateTime.of(
                            proximaCita.getFecha(), proximaCita.getHora());
                    if (fechaHora.isBefore(fechaProxima)) {
                        proximaCita = cita;
                    }
                }
            }
        }
        return Optional.ofNullable(proximaCita);
    }

    @Transactional(readOnly = true)
    public List<Cita> getCitasPorDoctor(Doctor doctor) {
        return citaRepository.findByDoctorOrderByFechaAscHoraAsc(doctor);
    }

    @Transactional(readOnly = true)
    public List<Cita> getAgendaDoctor(Doctor doctor, LocalDate fecha) {
        return citaRepository.findByDoctorAndFechaOrderByHoraAsc(doctor, fecha);
    }

    @Transactional(readOnly = true)
    public List<Cita> getAgendaDoctor(Doctor doctor, LocalDate inicio, LocalDate fin) {
        return citaRepository.findByDoctorAndFechaBetweenOrderByFechaAscHoraAsc(doctor, inicio, fin);
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getHorasDisponibles(Doctor doctor, LocalDate fecha) {
        // Se generan espacios de 30 minutos dentro del horario del doctor.
        var disponibles = new ArrayList<LocalTime>();
        var diaSemana = convertirDia(fecha.getDayOfWeek());
        var horarios = horarioRepository.findByDoctorAndDiaSemana(doctor, diaSemana);
        for (var horario : horarios) {
            var hora = horario.getHoraInicio();
            while (hora.isBefore(horario.getHoraFin())) {
                var fechaHora = LocalDateTime.of(fecha, hora);
                var citas = citaRepository.findByDoctorAndFechaAndHora(doctor, fecha, hora);
                var bloqueos = horarioBloqueadoRepository
                        .findByDoctorAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                doctor, fechaHora, fechaHora);
                boolean ocupado = false;
                for (var cita : citas) {
                    if (cita.getEstado() != EstadoCita.CANCELADA) {
                        ocupado = true;
                    }
                }
                if (!ocupado && bloqueos.isEmpty() && fechaHora.isAfter(LocalDateTime.now())) {
                    disponibles.add(hora);
                }
                hora = hora.plusMinutes(30);
            }
        }
        return disponibles;
    }

    @Transactional(readOnly = true)
    public List<Cita> getCitasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(fechaInicio, fechaFin);
    }

    @Transactional
    public Cita reservar(Cita cita) {
        // No se permite reservar dos citas activas en el mismo horario.
        var citasExistentes = citaRepository.findByDoctorAndFechaAndHora(
                cita.getDoctor(), cita.getFecha(), cita.getHora());
        for (var citaExistente : citasExistentes) {
            if (citaExistente.getEstado() != EstadoCita.CANCELADA) {
                throw new IllegalStateException("El horario seleccionado ya tiene una cita registrada.");
            }
        }
        // También se revisan los bloqueos registrados por el doctor.
        var fechaHora = LocalDateTime.of(cita.getFecha(), cita.getHora());
        var bloqueos = horarioBloqueadoRepository
                .findByDoctorAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        cita.getDoctor(), fechaHora, fechaHora);
        if (!bloqueos.isEmpty()) {
            throw new IllegalStateException("El horario seleccionado no está disponible.");
        }
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setFechaCreacion(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    @Transactional
    public void confirmar(Integer idCita) {
        var cita = getCitaPendiente(idCita);
        cita.setEstado(EstadoCita.CONFIRMADA);
        citaRepository.save(cita);
        notificacionService.notificarCambioEstado(cita, "CONFIRMADA");
    }

    @Transactional
    public void rechazar(Integer idCita, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo del rechazo.");
        }
        var cita = getCitaPendiente(idCita);
        cita.setEstado(EstadoCita.CANCELADA);
        cita.setNotas("Motivo de rechazo: " + motivo);
        citaRepository.save(cita);
        notificacionService.notificarCambioEstado(cita, "RECHAZADA");
    }

    @Transactional
    public void cancelar(Integer idCita, Paciente paciente) {
        // Se valida la propiedad de la cita antes de aplicar la cancelación.
        var citaOpt = citaRepository.findById(idCita);
        if (citaOpt.isEmpty() || !citaOpt.get().getPaciente().getIdPaciente().equals(paciente.getIdPaciente())) {
            throw new IllegalStateException("La cita no pertenece al paciente en sesión.");
        }
        var cita = citaOpt.get();
        if (cita.getEstado() == EstadoCita.CANCELADA
                || cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new IllegalStateException("La cita ya no se puede cancelar.");
        }
        var fechaHora = LocalDateTime.of(cita.getFecha(), cita.getHora());
        if (!fechaHora.isAfter(LocalDateTime.now().plusHours(24))) {
            throw new IllegalStateException("La cita solo puede cancelarse con más de 24 horas de anticipación.");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
        notificacionService.notificarCambioEstado(cita, "CANCELADA");
    }

    @Transactional
    public void completar(Integer idCita, Doctor doctor, String notas) {
        var citaOpt = citaRepository.findById(idCita);
        if (citaOpt.isPresent()) {
            var cita = citaOpt.get();
            if (!cita.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
                throw new IllegalStateException("La cita no pertenece al doctor en sesión.");
            }
            if (cita.getEstado() == EstadoCita.CANCELADA
                    || cita.getEstado() == EstadoCita.COMPLETADA) {
                throw new IllegalStateException("La cita ya no se puede completar.");
            }
            cita.setEstado(EstadoCita.COMPLETADA);
            cita.setNotas(notas);
            citaRepository.save(cita);
        }
    }

    @Transactional
    public void reprogramar(Integer idCita, LocalDate nuevaFecha, LocalTime nuevaHora, Doctor nuevoDoctor) {
        // El administrador puede mover una cita a otra fecha, hora o médico.
        var cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("La cita no existe."));
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new IllegalStateException("La cita ya no se puede reprogramar.");
        }
        var citasExistentes = citaRepository.findByDoctorAndFechaAndHora(nuevoDoctor, nuevaFecha, nuevaHora);
        for (var citaExistente : citasExistentes) {
            if (!citaExistente.getIdCita().equals(idCita) && citaExistente.getEstado() != EstadoCita.CANCELADA) {
                throw new IllegalStateException("El nuevo horario ya tiene una cita registrada.");
            }
        }
        // El horario anterior queda disponible porque la disponibilidad se calcula a partir de esta misma cita.
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setDoctor(nuevoDoctor);
        cita.setEstado(EstadoCita.CONFIRMADA);
        citaRepository.save(cita);
        notificacionService.notificarCambioEstado(cita, "REPROGRAMADA");
    }

    private Cita getCitaPendiente(Integer idCita) {
        var cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("La cita no existe."));
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new IllegalStateException("La cita ya fue gestionada.");
        }
        return cita;
    }

    private com.citamed.domain.DiaSemana convertirDia(DayOfWeek dayOfWeek) {
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return com.citamed.domain.DiaSemana.LUNES;
        }
        if (dayOfWeek == DayOfWeek.TUESDAY) {
            return com.citamed.domain.DiaSemana.MARTES;
        }
        if (dayOfWeek == DayOfWeek.WEDNESDAY) {
            return com.citamed.domain.DiaSemana.MIERCOLES;
        }
        if (dayOfWeek == DayOfWeek.THURSDAY) {
            return com.citamed.domain.DiaSemana.JUEVES;
        }
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            return com.citamed.domain.DiaSemana.VIERNES;
        }
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return com.citamed.domain.DiaSemana.SABADO;
        }
        return com.citamed.domain.DiaSemana.DOMINGO;
    }
}
