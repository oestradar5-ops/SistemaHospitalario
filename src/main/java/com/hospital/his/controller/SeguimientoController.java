package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.SeguimientoMedico;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.SeguimientoMedicoRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class SeguimientoController {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final CitaRepository citaRepository;

    private final SeguimientoMedicoRepository
            seguimientoMedicoRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public SeguimientoController(
            CitaRepository citaRepository,
            SeguimientoMedicoRepository seguimientoMedicoRepository) {

        this.citaRepository = citaRepository;
        this.seguimientoMedicoRepository = seguimientoMedicoRepository;
    }

    // ==========================================
    // VALIDAR SESIÓN DEL MÉDICO
    // ==========================================

    private boolean esMedico(HttpSession session) {

        String usuario = (String) session.getAttribute("usuario");
        String rol = (String) session.getAttribute("rol");

        return usuario != null
                && "MEDICO".equalsIgnoreCase(rol);
    }

    // ==========================================
    // VALIDAR QUE LA CITA PERTENECE AL MÉDICO
    // ==========================================

    private boolean perteneceAlMedico(
            Cita cita,
            String nombreUsuario) {

        return nombreUsuario != null
                && cita.getMedico() != null
                && cita.getMedico().getUsuario() != null
                && nombreUsuario.equalsIgnoreCase(
                cita.getMedico()
                        .getUsuario()
                        .getNombreUsuario()
        );
    }

    // ==========================================
    // MOSTRAR FORMULARIO DE SEGUIMIENTO
    // CU-08 FA02
    // ==========================================

    @GetMapping("/medico/seguimiento/{id}")
    public String mostrarSeguimiento(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        Optional<Cita> citaOptional = citaRepository.findById(id);

        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita seleccionada no existe."
            );

            return "redirect:/medico";
        }

        Cita cita = citaOptional.get();

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(cita, nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para programar seguimientos de esta cita."
            );

            return "redirect:/medico";
        }

        if (!"EVALUADO".equalsIgnoreCase(cita.getEstado())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita debe estar evaluada para programar un seguimiento."
            );

            return "redirect:/medico";
        }

        model.addAttribute("cita", cita);

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );

        model.addAttribute(
                "fechaMinima",
                LocalDate.now().plusDays(1)
        );

        model.addAttribute(
                "seguimientos",
                seguimientoMedicoRepository
                        .findByCitaIdOrderByFechaCreacionDesc(id)
        );

        return "medico/seguimiento";
    }

    // ==========================================
    // GUARDAR SEGUIMIENTO MÉDICO
    // CU-08 FA02
    // ==========================================

    @PostMapping("/medico/seguimiento/{id}/guardar")
    public String guardarSeguimiento(
            @PathVariable Long id,

            @RequestParam("fechaSeguimiento")
            LocalDate fechaSeguimiento,

            @RequestParam("motivo")
            String motivo,

            @RequestParam(
                    name = "observaciones",
                    required = false,
                    defaultValue = ""
            )
            String observaciones,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // VALIDAR SESIÓN

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        // BUSCAR CITA

        Optional<Cita> citaOptional = citaRepository.findById(id);

        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita seleccionada no existe."
            );

            return "redirect:/medico";
        }

        Cita cita = citaOptional.get();

        // VALIDAR MÉDICO

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(cita, nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para registrar seguimientos de esta cita."
            );

            return "redirect:/medico";
        }

        // VALIDAR ESTADO DE LA CITA

        if (!"EVALUADO".equalsIgnoreCase(cita.getEstado())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita debe estar evaluada para registrar un seguimiento."
            );

            return "redirect:/medico";
        }

        // VALIDAR FECHA

        if (fechaSeguimiento == null
                || !fechaSeguimiento.isAfter(LocalDate.now())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La fecha de seguimiento debe ser posterior a la fecha actual."
            );

            return "redirect:/medico/seguimiento/" + id;
        }

        // VALIDAR MOTIVO

        if (motivo == null || motivo.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debe ingresar el motivo del seguimiento."
            );

            return "redirect:/medico/seguimiento/" + id;
        }

        if (motivo.trim().length() > 250) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El motivo no puede superar los 250 caracteres."
            );

            return "redirect:/medico/seguimiento/" + id;
        }

        // CREAR SEGUIMIENTO

        SeguimientoMedico seguimiento =
                new SeguimientoMedico();

        seguimiento.setCita(cita);

        seguimiento.setFechaSeguimiento(fechaSeguimiento);

        seguimiento.setMotivo(motivo.trim());

        seguimiento.setObservaciones(
                observaciones == null
                        ? ""
                        : observaciones.trim()
        );

        seguimiento.setEstado("PROGRAMADO");

        // GUARDAR EN POSTGRESQL

        SeguimientoMedico seguimientoGuardado =
                seguimientoMedicoRepository.save(seguimiento);

        // CONFIRMACIÓN

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Seguimiento médico #"
                        + seguimientoGuardado.getId()
                        + " registrado correctamente para el "
                        + fechaSeguimiento + "."
        );

        // REGRESAR AL FORMULARIO PARA VER EL HISTORIAL

        return "redirect:/medico/seguimiento/" + id;
    }
}