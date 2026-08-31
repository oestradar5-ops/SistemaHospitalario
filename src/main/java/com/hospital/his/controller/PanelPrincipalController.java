package com.hospital.his.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PanelPrincipalController {


    // =====================================================
    // PANEL PRINCIPAL DEL PERSONAL INTERNO
    // =====================================================

    @GetMapping("/panel")
    public String mostrarPanel(
            HttpSession session,
            Model model) {


        String usuario =
                (String) session.getAttribute("usuario");


        String rol =
                (String) session.getAttribute("rol");


        // =============================================
        // VALIDAR SESIÓN
        // =============================================

        if (usuario == null ||
                rol == null) {

            return "redirect:/admin/login";
        }


        // =============================================
        // NO PERMITIR PACIENTES
        // =============================================

        if ("PACIENTE".equalsIgnoreCase(rol)) {

            return "redirect:/paciente/dashboard";
        }


        // =============================================
        // DATOS PARA EL HTML
        // =============================================

        model.addAttribute(
                "usuario",
                usuario
        );


        model.addAttribute(
                "rol",
                rol
        );


        return "panel/index";
    }
}
