package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.repository.CitaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class MedicoController {

    private final CitaRepository citaRepository;

    public MedicoController(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    // ==========================================
    // VERIFICAR SESIÓN DEL MÉDICO
    // ==========================================

    private boolean esMedico(HttpSession session) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        return usuario != null
                && rol != null
                && "MEDICO".equalsIgnoreCase(rol);
    }


    // ==========================================
    // PANEL PRINCIPAL DEL MÉDICO
    // ==========================================

    @GetMapping("/medico")
    public String mostrarPanel(
            HttpSession session,
            Model model) {

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        // Buscar todos los pacientes que ya terminaron
        // la toma de signos vitales
        List<Cita> citas =
                citaRepository
                        .findByEstadoOrderByFechaCreacionAsc(
                                "EN_ESPERA"
                        );

        // Mostrar solamente las citas del médico
        // que inició sesión
        List<Cita> citasDelMedico =
                citas.stream()

                        .filter(cita ->
                                cita.getMedico() != null
                                        && cita.getMedico().getUsuario() != null
                                        && nombreUsuario.equalsIgnoreCase(
                                        cita.getMedico()
                                                .getUsuario()
                                                .getNombreUsuario()
                                )
                        )

                        // EMERGENCIA primero
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                (Cita cita) ->
                                                        "EMERGENCIA"
                                                                .equalsIgnoreCase(
                                                                        cita.getPrioridad()
                                                                )
                                                                ? 0
                                                                : 1
                                        )
                                        .thenComparing(
                                                cita ->
                                                        cita.getHoraLlegada() == null
                                                                ? cita.getFechaCreacion()
                                                                : cita.getHoraLlegada()
                                        )
                        )

                        .toList();

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );

        model.addAttribute(
                "citas",
                citasDelMedico
        );

        return "medico/index";
    }
}