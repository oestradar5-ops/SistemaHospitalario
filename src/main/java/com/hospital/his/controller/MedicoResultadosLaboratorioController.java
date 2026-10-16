package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.OrdenExamen;
import com.hospital.his.entity.OrdenLaboratorio;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.OrdenLaboratorioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MedicoResultadosLaboratorioController {

    private final CitaRepository citaRepository;

    private final OrdenLaboratorioRepository ordenRepository;

    public MedicoResultadosLaboratorioController(
            CitaRepository citaRepository,
            OrdenLaboratorioRepository ordenRepository) {

        this.citaRepository = citaRepository;
        this.ordenRepository = ordenRepository;
    }

    // ==========================================
    // CU-09: VERIFICAR SESIÓN DEL MÉDICO
    // ==========================================

    private boolean esMedico(HttpSession session) {

        Object usuario = session.getAttribute("usuario");
        Object rol = session.getAttribute("rol");

        return usuario instanceof String
                && rol instanceof String
                && "MEDICO".equalsIgnoreCase((String) rol);
    }

    // ==========================================
    // CU-09: VERIFICAR MÉDICO RESPONSABLE
    // ==========================================

    private boolean perteneceAlMedico(
            Cita cita,
            String nombreUsuario) {

        return cita != null
                && nombreUsuario != null
                && cita.getMedico() != null
                && cita.getMedico().getUsuario() != null
                && nombreUsuario.equalsIgnoreCase(
                cita.getMedico()
                        .getUsuario()
                        .getNombreUsuario()
        );
    }

    // ==========================================
    // CU-09: CONSULTAR RESULTADOS POR CITA
    // ==========================================

    @Transactional(readOnly = true)
    @GetMapping("/medico/cita/{citaId}/laboratorio")
    public String consultarResultados(
            @PathVariable Long citaId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // 1. VERIFICAR SESIÓN
        // ======================================

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        // ======================================
        // 2. BUSCAR CITA
        // ======================================

        Optional<Cita> citaOptional =
                citaRepository.findById(citaId);

        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita médica no existe."
            );

            return "redirect:/medico";
        }

        Cita cita = citaOptional.get();

        // ======================================
        // 3. VERIFICAR MÉDICO RESPONSABLE
        // ======================================

        if (!perteneceAlMedico(cita, nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para consultar "
                            + "los resultados de esta cita."
            );

            return "redirect:/medico";
        }

        // ======================================
        // 4. BUSCAR ÓRDENES DE LA CITA
        // ======================================

        List<OrdenLaboratorio> ordenes =
                ordenRepository
                        .findByCitaIdOrderByFechaCreacionDesc(citaId);

        // ======================================
        // 5. PREPARAR RESULTADOS PUBLICADOS
        // ======================================

        List<Map<String, Object>> ordenesPublicadas =
                new ArrayList<>();

        for (OrdenLaboratorio orden : ordenes) {

            List<Map<String, Object>> resultados =
                    new ArrayList<>();

            if (orden.getExamenes() == null) {
                continue;
            }

            for (OrdenExamen examen : orden.getExamenes()) {

                // SOLO MOSTRAR RESULTADOS PUBLICADOS

                if (!Boolean.TRUE.equals(
                        examen.getPublicado())) {
                    continue;
                }

                Map<String, Object> resultado =
                        new LinkedHashMap<>();

                resultado.put(
                        "nombre",
                        examen.getExamen() != null
                                ? examen.getExamen().getNombre()
                                : "Examen"
                );

                resultado.put(
                        "valor",
                        examen.getValorResultado()
                );

                resultado.put(
                        "unidad",
                        examen.getUnidad()
                );

                resultado.put(
                        "notas",
                        examen.getNotasResultado()
                );

                resultado.put(
                        "fueraRango",
                        Boolean.TRUE.equals(
                                examen.getFueraRango()
                        )
                );

                resultado.put(
                        "fechaPublicacion",
                        examen.getFechaPublicacion()
                );

                resultados.add(resultado);
            }

            // NO MOSTRAR ÓRDENES SIN RESULTADOS PUBLICADOS

            if (resultados.isEmpty()) {
                continue;
            }

            Map<String, Object> ordenPublicada =
                    new LinkedHashMap<>();

            ordenPublicada.put("id", orden.getId());

            ordenPublicada.put(
                    "estado",
                    orden.getEstado()
            );

            ordenPublicada.put(
                    "fecha",
                    orden.getFechaCreacion()
            );

            ordenPublicada.put(
                    "resultados",
                    resultados
            );

            ordenesPublicadas.add(ordenPublicada);
        }

        // ======================================
        // 6. ENVIAR DATOS A LA VISTA
        // ======================================

        model.addAttribute("citaId", citaId);

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );

        model.addAttribute(
                "ordenesPublicadas",
                ordenesPublicadas
        );

        // ======================================
        // 7. MOSTRAR PANTALLA
        // ======================================

        return "medico/resultados-laboratorio";
    }
}