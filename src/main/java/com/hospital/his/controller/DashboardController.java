package com.hospital.his.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/paciente/dashboard")
    public String mostrarDashboard(
            HttpSession session,
            Model model) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        if (usuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return "redirect:/login";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        return "paciente/dashboard";
    }
}