package com.hospital.his.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardPagosVistaController {

    @GetMapping("/dashboards/pagos")
    public String mostrarDashboardPagos() {

        return "dashboard-pagos";
    }
}