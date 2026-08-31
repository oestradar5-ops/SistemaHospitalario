package com.hospital.his.service;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.Pago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ComprobanteService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoEmisor;


    public ComprobanteService(
            JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }


    public void enviarComprobante(
            Cita cita,
            Pago pago) {

        if (cita == null ||
                cita.getPaciente() == null ||
                cita.getPaciente().getCorreoElectronico() == null ||
                cita.getPaciente().getCorreoElectronico().isBlank()) {

            return;
        }


        String destinatario =
                cita.getPaciente()
                        .getCorreoElectronico();


        String medico =
                cita.getMedico()
                        .getUsuario()
                        .getNombreCompleto();


        String especialidad =
                cita.getEspecialidad()
                        .getNombre();


        String sucursal =
                cita.getSucursal()
                        .getNombre();


        String fecha =
                cita.getHorario()
                        .getFecha()
                        .toString();


        String hora =
                cita.getHorario()
                        .getHora()
                        .toString();


        String contenido =
                "SISTEMA INFORMÁTICO HOSPITALARIO\n\n" +

                        "COMPROBANTE DE PAGO\n\n" +

                        "Estimado(a) " +
                        cita.getPaciente().getNombreCompleto() +
                        ":\n\n" +

                        "Su pago ha sido procesado correctamente y su cita médica ha sido confirmada.\n\n" +

                        "------------------------------------------\n" +
                        "DATOS DE LA TRANSACCIÓN\n" +
                        "------------------------------------------\n\n" +

                        "Número de transacción: " +
                        pago.getReferencia() + "\n" +

                        "Monto pagado: Q" +
                        pago.getMonto() + "\n" +

                        "Método de pago: " +
                        pago.getMetodoPago() + "\n" +

                        "Estado del pago: " +
                        pago.getEstado() + "\n\n" +

                        "------------------------------------------\n" +
                        "DATOS DE LA CITA\n" +
                        "------------------------------------------\n\n" +

                        "Médico: " +
                        medico + "\n" +

                        "Especialidad: " +
                        especialidad + "\n" +

                        "Sucursal: " +
                        sucursal + "\n" +

                        "Fecha: " +
                        fecha + "\n" +

                        "Hora: " +
                        hora + "\n\n" +

                        "Su cita se encuentra CONFIRMADA.\n\n" +

                        "Gracias por utilizar el Sistema Informático Hospitalario.";


        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setFrom(
                correoEmisor
        );

        mensaje.setTo(
                destinatario
        );

        mensaje.setSubject(
                "Comprobante de pago - Cita médica confirmada"
        );

        mensaje.setText(
                contenido
        );


        mailSender.send(
                mensaje
        );
    }
}