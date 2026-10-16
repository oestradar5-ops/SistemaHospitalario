package com.hospital.his.controller;

import com.hospital.his.service.LaboratorioResultadosService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LaboratorioResultadosController {

    private final LaboratorioResultadosService resultadosService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public LaboratorioResultadosController(
            LaboratorioResultadosService resultadosService) {

        this.resultadosService = resultadosService;
    }

    // ==========================================
    // VALIDAR SESIÓN DE LABORATORIO
    // ==========================================

    private boolean esLaboratorio(HttpSession session) {

        Object usuario = session.getAttribute("usuario");

        Object rol = session.getAttribute("rol");

        return usuario != null
                && rol instanceof String
                && "LABORATORIO".equalsIgnoreCase((String) rol);
    }

    // ==========================================
    // CU-09: GUARDAR RESULTADO DE UN EXAMEN
    // ==========================================

    @PostMapping("/laboratorio/orden/{ordenId}/resultado/{examenId}")
    public String guardarResultado(
            @PathVariable Long ordenId,
            @PathVariable Long examenId,

            @RequestParam("valorResultado")
            String valorResultado,

            @RequestParam(
                    name = "unidad",
                    required = false,
                    defaultValue = ""
            )
            String unidad,

            @RequestParam(
                    name = "notasResultado",
                    required = false,
                    defaultValue = ""
            )
            String notasResultado,

            @RequestParam(
                    name = "fueraRango",
                    required = false,
                    defaultValue = "false"
            )
            boolean fueraRango,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // 1. VALIDAR SESIÓN
        // ======================================

        if (!esLaboratorio(session)) {

            return "redirect:/laboratorio/login";
        }

        // ======================================
        // 2. VALIDAR Y GUARDAR RESULTADO
        // ======================================

        try {

            resultadosService.registrarResultado(
                    ordenId,
                    examenId,
                    valorResultado,
                    unidad,
                    notasResultado,
                    fueraRango
            );

            // ==================================
            // 3. MENSAJE DE CONFIRMACIÓN
            // ==================================

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El resultado del examen se guardó correctamente."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            // ==================================
            // 4. MENSAJE DE ERROR
            // ==================================

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        // ======================================
        // 5. REGRESAR AL DETALLE DE LA ORDEN
        // ======================================

        return "redirect:/laboratorio/orden/" + ordenId;
    }
}