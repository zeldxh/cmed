package com.citamed.service;

import com.citamed.domain.Cita;
import com.citamed.domain.Notificacion;
import com.citamed.repository.NotificacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    // Envía y registra los correos generados por cambios en el ciclo de vida de una cita.
    private final NotificacionRepository notificacionRepository;
    private final CorreoService correoService;
    private final MessageSource messageSource;

    public NotificacionService(NotificacionRepository notificacionRepository,
            CorreoService correoService, MessageSource messageSource) {
        this.notificacionRepository = notificacionRepository;
        this.correoService = correoService;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> getNotificaciones() {
        return notificacionRepository.findAllByOrderByFechaEnvioDesc();
    }

    @Transactional
    public void notificarCambioEstado(Cita cita, String tipo) {
        String destinatario = cita.getPaciente().getUsuario().getCorreo();
        String asunto = messageSource.getMessage("notificacion.asunto." + tipo.toLowerCase(),
                null, LocaleContextHolder.getLocale());
        String cuerpo = messageSource.getMessage("notificacion.cuerpo." + tipo.toLowerCase(),
                new Object[]{cita.getPaciente().getUsuario().getNombre(), cita.getDoctor().getUsuario().getNombre(),
                    cita.getFecha(), cita.getHora()},
                LocaleContextHolder.getLocale());

        boolean exito = correoService.enviar(destinatario, asunto, cuerpo);

        var notificacion = new Notificacion();
        notificacion.setCita(cita);
        notificacion.setTipo(tipo);
        notificacion.setDestinatario(destinatario);
        notificacion.setExito(exito);
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }
}
