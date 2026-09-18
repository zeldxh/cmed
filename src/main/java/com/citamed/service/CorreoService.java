package com.citamed.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    // Envoltorio simple sobre JavaMailSender para las notificaciones de citas.
    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoService(JavaMailSender mailSender,
            @Value("${spring.mail.username:citamed@localhost}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public boolean enviar(String destinatario, String asunto, String cuerpo) {
        try {
            var mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            return true;
        } catch (MailException e) {
            return false;
        }
    }
}
