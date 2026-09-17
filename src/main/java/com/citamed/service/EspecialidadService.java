package com.citamed.service;

import com.citamed.domain.Especialidad;
import com.citamed.repository.DoctorRepository;
import com.citamed.repository.EspecialidadRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadService {

    // Operaciones básicas del catálogo de especialidades médicas.
    private final EspecialidadRepository especialidadRepository;
    private final DoctorRepository doctorRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository, DoctorRepository doctorRepository) {
        this.especialidadRepository = especialidadRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> getEspecialidades(boolean activo) {
        // Permite recuperar todas las especialidades o solamente las activas.
        if (activo) {
            return especialidadRepository.findByActivoTrue();
        }
        return especialidadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Especialidad> getEspecialidad(Integer idEspecialidad) {
        return especialidadRepository.findById(idEspecialidad);
    }

    @Transactional
    public Especialidad save(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }

    @Transactional
    public void delete(Integer idEspecialidad) {
        var especialidad = especialidadRepository.findById(idEspecialidad)
                .orElseThrow(() -> new IllegalArgumentException("La especialidad no existe."));
        if (!doctorRepository.findByEspecialidad(especialidad).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar una especialidad con médicos asignados.");
        }
        especialidadRepository.delete(especialidad);
    }
}
