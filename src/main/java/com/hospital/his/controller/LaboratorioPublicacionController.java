package com.hospital.his.controller;

import com.hospital.his.service.LaboratorioResultadosService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LaboratorioPublicacionController {

    private final LaboratorioResultadosService resultadosService;

    public LaboratorioPublicacionController(
            LaboratorioResultadosService resultadosService) {

        this.resultadosService = resultadosService;
    }

    // ==========================================
    // CU-09: PUBLICAR RESULTADOS DE LABORATORIO
    // ==========================================

    @PostMapping("/laboratorio/orden/{ordenId}/publicar")
    public String publicarResultados(
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
        // 2. PUBLICAR RESULTADOS
        // ======================================

        try {

            resultadosService.publicarResultados(ordenId);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Los resultados de laboratorio "
                            + "se publicaron correctamente."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }

        // ======================================
        // 3. REGRESAR AL DETALLE DE LA ORDEN
        // ======================================

        return "redirect:/laboratorio/orden/" + ordenId;
    }
}