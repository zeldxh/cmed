package com.citamed.repository;

import com.citamed.domain.Doctor;
import com.citamed.domain.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    // Consultas derivadas para doctores activos, especialidad y usuario.
    public List<Doctor> findByActivoTrue();

    public List<Doctor> findByEspecialidadAndActivoTrue(Especialidad especialidad);

    public List<Doctor> findByEspecialidad(Especialidad especialidad);

    public Optional<Doctor> findByUsuarioIdUsuario(Integer idUsuario);
}
