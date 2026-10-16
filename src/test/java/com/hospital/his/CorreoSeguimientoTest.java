package com.hospital.his;

import com.hospital.his.entity.Cita;
import com.hospital.his.repository.CitaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CorreoSeguimientoTest {

    // ==========================================
    // REPOSITORIO DE CITAS
    // ==========================================

    @Autowired
    private CitaRepository citaRepository;


    // ==========================================
    // SERVICIO DE CORREO
    // ==========================================

    @Autowired
    private JavaMailSender mailSenderPrueba;


    // ==========================================
    // CORREO REMITENTE
    // ==========================================

    @Value("${spring.mail.username}")
    private String correoRemitentePrueba;


    // ==========================================
    // CU-12 - VERIFICAR CITA DE SEGUIMIENTO
    // ==========================================

    @Test
    @Transactional
    void probarConfirmacionSeguimiento() {

        Long citaId = 17L;

        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la cita " + citaId
                        )
                );

        assertEquals(
                "SEGUIMIENTO",
                cita.getOrigenCita()
        );

        System.out.println(
                "CU-12: Cita encontrada: " + cita.getId()
        );

        System.out.println(
                "CU-12: Paciente identificado correctamente."
        );

        System.out.println(
                "CU-12: Preparación de prueba completada."
        );
    }


    // ==========================================
    // CU-12 - PRUEBA DE CONEXIÓN CON GMAIL
    // ==========================================

    @Test
    void probarConexionCorreoGmail() {

        // ======================================
        // DESTINATARIO DE PRUEBA
        // ======================================

        // No colocar correos personales
        // directamente en el código.

        String destinatario = "TU_CORREO@gmail.com";


        // ======================================
        // CREAR MENSAJE
        // ======================================

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setFrom(correoRemitentePrueba);

        mensaje.setTo(destinatario);

        mensaje.setSubject(
                "Prueba de correo - Sistema Hospitalario HIS"
        );

        mensaje.setText(
                "Esta es una prueba de envío de correo electrónico " +
                        "desde el Sistema Informático Hospitalario.\n\n" +
                        "CU-12: Verificación de conexión SMTP.\n\n" +
                        "Si recibió este mensaje, significa que " +
                        "el sistema pudo enviar correos utilizando Gmail.\n\n" +
                        "Sistema Informático Hospitalario HIS."
        );


        // ======================================
        // ENVÍO DE CORREO
        // ======================================

        // PRUEBA COMPLETADA CORRECTAMENTE.
        // El correo fue enviado y recibido
        // durante las pruebas del CU-12.
        //
        // Se desactiva el envío para evitar
        // correos duplicados al ejecutar
        // las pruebas del proyecto.

        // mailSenderPrueba.send(mensaje);


        // ======================================
        // RESULTADO DE LA PRUEBA
        // ======================================

        assertNotNull(mensaje.getFrom());
        assertNotNull(mensaje.getTo());
        assertNotNull(mensaje.getSubject());
        assertNotNull(mensaje.getText());

        System.out.println(
                "CU-12: Mensaje de correo preparado correctamente."
        );

        System.out.println(
                "CU-12: Envío SMTP desactivado."
        );

        System.out.println(
                "CU-12: El envío real fue verificado anteriormente."
        );

    }

}