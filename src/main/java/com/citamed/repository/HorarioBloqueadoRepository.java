package com.citamed.repository;

import com.citamed.domain.Doctor;
import com.citamed.domain.HorarioBloqueado;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioBloqueadoRepository extends JpaRepository<HorarioBloqueado, Integer> {

    // Consulta los períodos bloqueados de la agenda de un doctor.
    public List<HorarioBloqueado> findByDoctorOrderByFechaInicioAsc(Doctor doctor);

    public List<HorarioBloqueado> findByDoctorAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Doctor doctor, LocalDateTime fechaFin, LocalDateTime fechaInicio);
}
