package com.citamed.repository;

import com.citamed.domain.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    // Consultas derivadas para recuperar y buscar pacientes.
    public List<Paciente> findByUsuarioActivoTrue();

    public Optional<Paciente> findByUsuarioIdUsuario(Integer idUsuario);

    public List<Paciente> findByUsuarioNombreContainingIgnoreCaseOrCedulaContainingIgnoreCase(
            String nombre, String cedula);
}
