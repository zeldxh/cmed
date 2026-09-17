package com.citamed.repository;

import com.citamed.domain.Doctor;
import com.citamed.domain.DiaSemana;
import com.citamed.domain.Horario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    // Recupera los bloques semanales registrados para cada doctor.
    public List<Horario> findByDoctor(Doctor doctor);

    public List<Horario> findByDoctorAndDiaSemana(Doctor doctor, DiaSemana diaSemana);
}
