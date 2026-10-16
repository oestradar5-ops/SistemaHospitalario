package com.hospital.his.service.impl;

import com.hospital.his.entity.Cita;
import com.hospital.his.service.CorreoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class CorreoServiceImpl implements CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    public CorreoServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ==========================================
    // CORREO DE BIENVENIDA
    // ==========================================

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

    // ==========================================
    // CU-12 - CONFIRMACIÓN DE SEGUIMIENTO
    // ==========================================

    @Override
    public void enviarConfirmacionSeguimiento(Cita cita) {

        String correo = cita.getPaciente().getCorreoElectronico();
        String nombre = cita.getPaciente().getNombreCompleto();

        String fecha = cita.getHorario().getFecha()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String hora = cita.getHorario().getHora()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        String medico = cita.getMedico()
                .getUsuario()
                .getNombreCompleto();

        String sucursal = cita.getSucursal().getNombre();

        String tipo = obtenerNombreTipoSeguimiento(
                cita.getTipoSeguimiento()
        );

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setFrom(correoRemitente);
        mensaje.setTo(correo);

        mensaje.setSubject(
                "Cita de seguimiento registrada - Hospital HIS"
        );

        mensaje.setText(
                "Estimado(a) " + nombre + ",\n\n" +
                        "Se ha registrado una cita de seguimiento médico.\n\n" +
                        "Número de cita: " + cita.getId() + "\n" +
                        "Tipo de seguimiento: " + tipo + "\n" +
                        "Fecha: " + fecha + "\n" +
                        "Hora: " + hora + "\n" +
                        "Médico: " + medico + "\n" +
                        "Sucursal: " + sucursal + "\n" +
                        "Motivo: " + cita.getMotivoConsulta() + "\n" +
                        "Estado: " + cita.getEstado() + "\n\n" +
                        "Por favor, consulte el estado de su cita " +
                        "en el portal del paciente.\n\n" +
                        "Sistema Informático Hospitalario HIS."
        );

        mailSender.send(mensaje);
    }

    // ==========================================
    // CU-12 - RECORDATORIO DE SEGUIMIENTO
    // ==========================================

    @Override
    public void enviarRecordatorioSeguimiento(Cita cita) {

        String correo = cita.getPaciente().getCorreoElectronico();
        String nombre = cita.getPaciente().getNombreCompleto();

        String fecha = cita.getHorario().getFecha()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String hora = cita.getHorario().getHora()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        String medico = cita.getMedico()
                .getUsuario()
                .getNombreCompleto();

        String sucursal = cita.getSucursal().getNombre();

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setFrom(correoRemitente);
        mensaje.setTo(correo);

        mensaje.setSubject(
                "Recordatorio de cita de seguimiento - Hospital HIS"
        );

        mensaje.setText(
                "Estimado(a) " + nombre + ",\n\n" +
                        "Le recordamos que tiene una cita de seguimiento médico.\n\n" +
                        "Fecha: " + fecha + "\n" +
                        "Hora: " + hora + "\n" +
                        "Médico: " + medico + "\n" +
                        "Sucursal: " + sucursal + "\n\n" +
                        "Le recomendamos presentarse puntualmente " +
                        "a su cita.\n\n" +
                        "Sistema Informático Hospitalario HIS."
        );

        mailSender.send(mensaje);
    }

    // ==========================================
    // OBTENER NOMBRE DEL TIPO DE SEGUIMIENTO
    // ==========================================

    private String obtenerNombreTipoSeguimiento(String tipo) {

        if ("MONITOREO_TRATAMIENTO".equals(tipo)) {
            return "Monitoreo de tratamiento";
        }

        if ("REVISION_RESULTADOS_LABORATORIO".equals(tipo)) {
            return "Revisión de resultados de laboratorio";
        }

        return "Seguimiento médico";
    }
}