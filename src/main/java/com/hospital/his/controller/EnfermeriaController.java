package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.repository.CitaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Controller
public class EnfermeriaController {

    private final CitaRepository citaRepository;


    public EnfermeriaController(
            CitaRepository citaRepository) {

        this.citaRepository =
                citaRepository;
    }


    // ==========================================
    // VALIDAR ENFERMERO
    // ==========================================

    private boolean esEnfermero(
            HttpSession session) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        return usuario != null
                && "ENFERMERO".equalsIgnoreCase(rol);
    }


    // ==========================================
    // PANEL DE ENFERMERÍA
    // ==========================================

    @GetMapping("/enfermeria")
    public String mostrarPanel(
            HttpSession session,
            Model model) {

        if (!esEnfermero(session)) {

            return "redirect:/enfermeria/login";
        }


        model.addAttribute(
                "usuarioActual",
                session.getAttribute("usuario")
        );


        return "enfermeria/index";
    }


    // ==========================================
    // PACIENTES PRESENTES
    // ==========================================

    @ResponseBody
    @GetMapping("/api/enfermeria/pacientes-presentes")
    public List<Map<String, Object>> pacientesPresentes(
            HttpSession session) {

        if (!esEnfermero(session)) {
            return List.of();
        }


        return citaRepository
                .findByEstadoOrderByFechaCreacionAsc(
                        "PACIENTE_PRESENTE"
                )
                .stream()
                .map(this::convertirCita)
                .toList();
    }


    // ==========================================
    // PACIENTES EN TOMA DE SIGNOS
    // ==========================================

    @ResponseBody
    @GetMapping("/api/enfermeria/en-signos")
    public List<Map<String, Object>> pacientesEnSignos(
            HttpSession session) {

        if (!esEnfermero(session)) {
            return List.of();
        }


        return citaRepository
                .findByEstadoOrderByFechaCreacionAsc(
                        "SIGNOS_VITALES"
                )
                .stream()
                .map(this::convertirCita)
                .toList();
    }


    // ==========================================
    // LLAMAR PACIENTE
    // ==========================================

    @ResponseBody
    @PostMapping(
            "/api/enfermeria/citas/{id}/llamar"
    )
    @Transactional
    public Map<String, Object> llamarPaciente(
            @PathVariable Long id,
            HttpSession session) {

        if (!esEnfermero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        Cita cita =
                citaRepository
                        .findById(id)
                        .orElse(null);


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        if (!"PACIENTE_PRESENTE"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El paciente ya no se encuentra pendiente de llamado."
            );
        }


        cita.setEstado(
                "SIGNOS_VITALES"
        );


        citaRepository.save(
                cita
        );


        return Map.of(
                "estado", "OK",

                "mensaje",
                "Paciente llamado correctamente.",

                "citaId",
                cita.getId(),

                "paciente",
                cita.getPaciente()
                        .getNombreCompleto(),

                "nuevoEstado",
                cita.getEstado()
        );
    }


    // ==========================================
    // CONVERTIR CITA A RESPUESTA
    // ==========================================

    private Map<String, Object> convertirCita(
            Cita cita) {

        String horaLlegada =
                cita.getHoraLlegada() == null
                        ? "No registrada"
                        : cita.getHoraLlegada()
                        .toString();


        return Map.ofEntries(

                Map.entry(
                        "citaId",
                        cita.getId()
                ),

                Map.entry(
                        "paciente",
                        cita.getPaciente()
                                .getNombreCompleto()
                ),

                Map.entry(
                        "dpi",
                        cita.getPaciente()
                                .getDpi()
                ),

                Map.entry(
                        "especialidad",
                        cita.getEspecialidad()
                                .getNombre()
                ),

                Map.entry(
                        "medico",
                        cita.getMedico()
                                .getUsuario()
                                .getNombreCompleto()
                ),

                Map.entry(
                        "estado",
                        cita.getEstado()
                ),

                Map.entry(
                        "prioridad",
                        cita.getPrioridad()
                ),

                Map.entry(
                        "horaLlegada",
                        horaLlegada
                )
        );
    }
}