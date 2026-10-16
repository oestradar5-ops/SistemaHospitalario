package com.hospital.his.controller;

import com.hospital.his.service.LaboratorioOrdenService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class LaboratorioPanelController {

    private final LaboratorioOrdenService laboratorioOrdenService;

    public LaboratorioPanelController(
            LaboratorioOrdenService laboratorioOrdenService) {

        this.laboratorioOrdenService = laboratorioOrdenService;
    }

    // ==========================================
    // CU-09: PANEL DE LABORATORIO
    // ==========================================

    @GetMapping("/laboratorio/panel")
    public String mostrarPanelLaboratorio(
            HttpSession session,
            Model model) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        // Validar acceso
        if (usuario == null ||
                !"LABORATORIO".equalsIgnoreCase(rol)) {

            return "redirect:/laboratorio/login";
        }

        // Consultar datos reales
        Map<String, Object> datos =
                laboratorioOrdenService.obtenerDatosPanel();

        // Enviar información al HTML
        model.addAllAttributes(datos);

        model.addAttribute(
                "usuarioLaboratorio",
                usuario
        );

        return "laboratorio/panel-laboratorio";
    }
}