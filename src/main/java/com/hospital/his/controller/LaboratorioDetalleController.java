package com.hospital.his.controller;

import com.hospital.his.service.LaboratorioOrdenService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Controller
public class LaboratorioDetalleController {

    private final LaboratorioOrdenService laboratorioOrdenService;

    public LaboratorioDetalleController(
            LaboratorioOrdenService laboratorioOrdenService) {

        this.laboratorioOrdenService = laboratorioOrdenService;
    }

    // ==========================================
    // CU-09: VER DETALLE DE UNA ORDEN
    // ==========================================

    @GetMapping("/laboratorio/orden/{id}")
    public String verDetalleOrden(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        // VALIDAR SESIÓN

        String usuario = (String) session.getAttribute("usuario");

        String rol = (String) session.getAttribute("rol");

        if (usuario == null ||
                !"LABORATORIO".equalsIgnoreCase(rol)) {

            return "redirect:/laboratorio/login";
        }

        // CONSULTAR ORDEN

        Map<String, Object> detalle;

        try {

            detalle = laboratorioOrdenService.obtenerDetalleOrden(id);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Orden de laboratorio no encontrada"
            );
        }

        // ENVIAR DATOS AL HTML

        model.addAttribute("orden", detalle);

        model.addAttribute("usuarioLaboratorio", usuario);

        return "laboratorio/detalle-orden";
    }
}