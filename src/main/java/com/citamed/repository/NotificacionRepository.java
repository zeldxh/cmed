package com.citamed.repository;

import com.citamed.domain.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // Consulta usada para revisar el historial de correos enviados.
    public List<Notificacion> findAllByOrderByFechaEnvioDesc();
}
