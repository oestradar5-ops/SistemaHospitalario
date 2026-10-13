package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.CatalogoMedicamento;
import com.hospital.his.entity.RecetaMedica;
import com.hospital.his.entity.RecetaMedicamento;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.CatalogoMedicamentoRepository;
import com.hospital.his.repository.RecetaMedicaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class RecetaController {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final CitaRepository citaRepository;

    private final CatalogoMedicamentoRepository
            catalogoMedicamentoRepository;

    private final RecetaMedicaRepository
            recetaMedicaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public RecetaController(
            CitaRepository citaRepository,
            CatalogoMedicamentoRepository catalogoMedicamentoRepository,
            RecetaMedicaRepository recetaMedicaRepository) {

        this.citaRepository = citaRepository;

        this.catalogoMedicamentoRepository =
                catalogoMedicamentoRepository;

        this.recetaMedicaRepository =
                recetaMedicaRepository;
    }


    // ==========================================
    // VALIDAR SESIÓN DEL MÉDICO
    // ==========================================

    private boolean esMedico(HttpSession session) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        return usuario != null
                && "MEDICO".equalsIgnoreCase(rol);
    }


    // ==========================================
    // VALIDAR QUE LA CITA PERTENECE AL MÉDICO
    // ==========================================

    private boolean perteneceAlMedico(
            Cita cita,
            String nombreUsuario) {

        return cita.getMedico() != null
                && cita.getMedico().getUsuario() != null
                && nombreUsuario.equalsIgnoreCase(
                cita.getMedico()
                        .getUsuario()
                        .getNombreUsuario()
        );
    }


    // ==========================================
    // MOSTRAR FORMULARIO DE RECETA MÉDICA
    // CU-08 FA04
    // ==========================================

    @GetMapping("/medico/receta/{id}")
    public String mostrarReceta(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // VALIDAR SESIÓN

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        // BUSCAR CITA

        Optional<Cita> citaOptional =
                citaRepository.findById(id);

        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita seleccionada no existe."
            );

            return "redirect:/medico";
        }

        Cita cita = citaOptional.get();

        // VALIDAR MÉDICO

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(cita, nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para generar recetas de esta cita."
            );

            return "redirect:/medico";
        }

        // VALIDAR ESTADO

        if (!"EVALUADO".equalsIgnoreCase(cita.getEstado())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita debe estar evaluada para generar una receta médica."
            );

            return "redirect:/medico";
        }

        // ENVIAR INFORMACIÓN AL FORMULARIO

        model.addAttribute("cita", cita);

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );

        model.addAttribute(
                "medicamentos",
                catalogoMedicamentoRepository
                        .findByActivoTrueOrderByNombreAsc()
        );

        return "medico/receta";
    }


    // ==========================================
    // GUARDAR RECETA MÉDICA
    // CU-08 FA04
    // ==========================================

    @PostMapping("/medico/receta/{id}/guardar")
    @Transactional
    public String guardarReceta(
            @PathVariable Long id,

            @RequestParam(
                    name = "medicamentoId",
                    required = false
            )
            List<Long> medicamentosIds,

            @RequestParam(
                    name = "dosis",
                    required = false
            )
            List<String> dosis,

            @RequestParam(
                    name = "frecuencia",
                    required = false
            )
            List<String> frecuencias,

            @RequestParam(
                    name = "duracion",
                    required = false
            )
            List<String> duraciones,

            @RequestParam(
                    name = "cantidad",
                    required = false
            )
            List<Integer> cantidades,

            @RequestParam(
                    name = "indicaciones",
                    required = false
            )
            List<String> indicaciones,

            @RequestParam(
                    name = "indicacionesGenerales",
                    required = false,
                    defaultValue = ""
            )
            String indicacionesGenerales,

            HttpSession session,

            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        // ======================================
        // BUSCAR CITA
        // ======================================

        Optional<Cita> citaOptional =
                citaRepository.findById(id);

        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita seleccionada no existe."
            );

            return "redirect:/medico";
        }

        Cita cita = citaOptional.get();

        // ======================================
        // VALIDAR MÉDICO
        // ======================================

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(cita, nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para generar recetas de esta cita."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR ESTADO
        // ======================================

        if (!"EVALUADO".equalsIgnoreCase(cita.getEstado())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita debe estar evaluada para generar una receta médica."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR MEDICAMENTOS
        // ======================================

        if (medicamentosIds == null
                || medicamentosIds.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debe seleccionar al menos un medicamento."
            );

            return "redirect:/medico/receta/" + id;
        }

        int total = medicamentosIds.size();

        // Verificar que las listas tengan
        // la misma cantidad de elementos.

        if (dosis == null
                || frecuencias == null
                || duraciones == null
                || cantidades == null
                || indicaciones == null
                || dosis.size() != total
                || frecuencias.size() != total
                || duraciones.size() != total
                || cantidades.size() != total
                || indicaciones.size() != total) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Los datos de los medicamentos están incompletos."
            );

            return "redirect:/medico/receta/" + id;
        }

        // ======================================
        // PREPARAR RECETA
        // ======================================

        RecetaMedica receta =
                new RecetaMedica();

        receta.setCita(cita);

        receta.setEstado("PENDIENTE");

        receta.setIndicacionesGenerales(
                indicacionesGenerales.trim()
        );

        List<RecetaMedicamento> detalles =
                new ArrayList<>();

        // ======================================
        // PROCESAR CADA MEDICAMENTO
        // ======================================

        for (int i = 0; i < total; i++) {

            Long medicamentoId =
                    medicamentosIds.get(i);

            String dosisActual =
                    dosis.get(i);

            String frecuenciaActual =
                    frecuencias.get(i);

            String duracionActual =
                    duraciones.get(i);

            Integer cantidadActual =
                    cantidades.get(i);

            String indicacionActual =
                    indicaciones.get(i);

            // VALIDAR CAMPOS OBLIGATORIOS

            if (medicamentoId == null
                    || dosisActual == null
                    || dosisActual.isBlank()
                    || frecuenciaActual == null
                    || frecuenciaActual.isBlank()
                    || duracionActual == null
                    || duracionActual.isBlank()
                    || cantidadActual == null
                    || cantidadActual <= 0) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Complete correctamente los datos del medicamento "
                                + (i + 1) + "."
                );

                return "redirect:/medico/receta/" + id;
            }

            // VALIDAR LONGITUD DE CAMPOS

            if (dosisActual.trim().length() > 100
                    || frecuenciaActual.trim().length() > 100
                    || duracionActual.trim().length() > 100) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "La dosis, frecuencia o duración supera "
                                + "el máximo de 100 caracteres."
                );

                return "redirect:/medico/receta/" + id;
            }

            // BUSCAR MEDICAMENTO

            Optional<CatalogoMedicamento> medicamentoOptional =
                    catalogoMedicamentoRepository
                            .findById(medicamentoId);

            if (medicamentoOptional.isEmpty()
                    || !Boolean.TRUE.equals(
                    medicamentoOptional.get().getActivo()
            )) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "El medicamento "
                                + (i + 1)
                                + " no existe o está inactivo."
                );

                return "redirect:/medico/receta/" + id;
            }

            // CREAR DETALLE

            RecetaMedicamento detalle =
                    new RecetaMedicamento();

            detalle.setReceta(receta);

            detalle.setMedicamento(
                    medicamentoOptional.get()
            );

            detalle.setDosis(
                    dosisActual.trim()
            );

            detalle.setFrecuencia(
                    frecuenciaActual.trim()
            );

            detalle.setDuracion(
                    duracionActual.trim()
            );

            detalle.setCantidad(
                    cantidadActual
            );

            detalle.setIndicaciones(
                    indicacionActual == null
                            ? ""
                            : indicacionActual.trim()
            );

            detalles.add(detalle);
        }

        // ======================================
        // ASOCIAR MEDICAMENTOS A LA RECETA
        // ======================================

        receta.setMedicamentos(detalles);

        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        RecetaMedica recetaGuardada =
                recetaMedicaRepository.saveAndFlush(receta);

        // ======================================
        // CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Receta médica #"
                        + recetaGuardada.getId()
                        + " registrada correctamente con "
                        + total
                        + " medicamento(s)."
        );

        // ======================================
        // REGRESAR AL PANEL MÉDICO
        // ======================================

        return "redirect:/medico";
    }
}