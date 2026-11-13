package com.hospital.his.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardFarmaciaVistaController {

    @GetMapping("/dashboards/farmacia")
    public String mostrarDashboardFarmacia() {

        return "dashboard-farmacia";
    }
}