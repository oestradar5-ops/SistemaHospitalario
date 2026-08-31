package com.hospital.his.service.impl;

import com.hospital.his.service.CorreoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoServiceImpl implements CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoRemitente;


    public CorreoServiceImpl(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }


    @Override
    public void enviarBienvenida(
            String correo,
            String nombre) {

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setFrom(correoRemitente);

        mensaje.setTo(correo);

        mensaje.setSubject(
                "Bienvenido al Sistema de Citas - Hospital HIS"
        );

        mensaje.setText(
                "Estimado(a) " + nombre + ",\n\n" +
                        "Su registro ha sido completado exitosamente. " +
                        "Ya puede agendar sus citas médicas a través de nuestro portal.\n\n" +
                        "Bienvenido al Sistema Informático Hospitalario."
        );

        mailSender.send(mensaje);
    }
}
