package com.hospital.his.controller;

import com.hospital.his.service.LaboratorioResultadosService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LaboratorioCompletarController {

    private final LaboratorioResultadosService resultadosService;

    public LaboratorioCompletarController(
            LaboratorioResultadosService resultadosService) {

        this.resultadosService = resultadosService;
    }

    // ==========================================
    // CU-09: COMPLETAR ORDEN DE LABORATORIO
    // ==========================================

    @PostMapping("/laboratorio/orden/{ordenId}/completar")
    public String completarOrden(
            @PathVariable Long ordenId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // 1. VERIFICAR SESIÓN
        // ======================================

        Object usuario = session.getAttribute("usuario");
        Object rol = session.getAttribute("rol");

        if (usuario == null
                || !(rol instanceof String)
                || !"LABORATORIO".equalsIgnoreCase((String) rol)) {

            return "redirect:/laboratorio/login";
        }

        // ======================================
        // 2. COMPLETAR ORDEN
        // ======================================

        try {

            resultadosService.completarOrden(ordenId);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La orden de laboratorio se completó correctamente."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }

        // ======================================
        // 3. REGRESAR AL DETALLE
        // ======================================

        return "redirect:/laboratorio/orden/" + ordenId;
    }
}