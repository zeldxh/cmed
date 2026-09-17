package com.citamed.repository;

import com.citamed.domain.Cita;
import com.citamed.domain.Doctor;
import com.citamed.domain.EstadoCita;
import com.citamed.domain.Paciente;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    // Consultas derivadas utilizadas por agendas, historiales y reportes.
    // Consultas derivadas para mostrar las citas en el orden de cada pantalla.
    public List<Cita> findByPacienteOrderByFechaDescHoraDesc(Paciente paciente);

    public List<Cita> findByDoctorOrderByFechaAscHoraAsc(Doctor doctor);

    public List<Cita> findByDoctorAndFechaOrderByHoraAsc(Doctor doctor, LocalDate fecha);

    public List<Cita> findByDoctorAndFechaBetweenOrderByFechaAscHoraAsc(
            Doctor doctor, LocalDate fechaInicio, LocalDate fechaFin);

    public List<Cita> findByFechaBetweenOrderByFechaAscHoraAsc(LocalDate fechaInicio, LocalDate fechaFin);

    public List<Cita> findByEstado(EstadoCita estado);

    // Se usa para comprobar si el horario ya fue reservado anteriormente.
    public List<Cita> findByDoctorAndFechaAndHora(Doctor doctor, LocalDate fecha, LocalTime hora);
}
