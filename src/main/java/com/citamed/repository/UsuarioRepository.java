package com.citamed.repository;

import com.citamed.domain.Rol;
import com.citamed.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Consultas derivadas para autenticación, roles y estado de las cuentas.
    public List<Usuario> findByActivoTrue();

    public List<Usuario> findByRolAndActivoTrue(Rol rol);

    public Optional<Usuario> findByCorreo(String correo);

    public boolean existsByCorreo(String correo);
}
