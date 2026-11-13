package com.hospital.his.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardVistaController {

    @GetMapping("/dashboards/medicos")
    public String mostrarDashboardMedicos() {
        return "dashboards/medicos";
    }
}