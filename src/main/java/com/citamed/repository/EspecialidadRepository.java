package com.citamed.repository;

import com.citamed.domain.Especialidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {

    // Recupera las especialidades habilitadas para médicos y reservas.
    public List<Especialidad> findByActivoTrue();
}
