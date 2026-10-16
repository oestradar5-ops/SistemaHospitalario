package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.CatalogoExamen;
import com.hospital.his.entity.OrdenExamen;
import com.hospital.his.entity.OrdenLaboratorio;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.CatalogoExamenRepository;
import com.hospital.his.repository.OrdenLaboratorioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class LaboratorioController {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final CitaRepository citaRepository;

    private final CatalogoExamenRepository catalogoExamenRepository;

    private final OrdenLaboratorioRepository ordenLaboratorioRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public LaboratorioController(
            CitaRepository citaRepository,
            CatalogoExamenRepository catalogoExamenRepository,
            OrdenLaboratorioRepository ordenLaboratorioRepository) {

        this.citaRepository = citaRepository;

        this.catalogoExamenRepository = catalogoExamenRepository;

        this.ordenLaboratorioRepository = ordenLaboratorioRepository;
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
    // MOSTRAR FORMULARIO DE LABORATORIO
    // CU-08 FA01
    // ==========================================

    @GetMapping("/medico/laboratorio/{id}")
    public String mostrarLaboratorio(
            @PathVariable Long id,
            HttpSession session,
            Model model,
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
                    "No tiene autorización para generar órdenes de esta cita."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR ESTADO
        // ======================================

        if (!"EVALUADO".equalsIgnoreCase(cita.getEstado())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita debe estar evaluada para generar una orden de laboratorio."
            );

            return "redirect:/medico";
        }

        // ======================================
        // ENVIAR INFORMACIÓN AL FORMULARIO
        // ======================================

        model.addAttribute("cita", cita);

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );

        model.addAttribute(
                "examenes",
                catalogoExamenRepository
                        .findByActivoTrueOrderByNombreAsc()
        );

        return "medico/laboratorio";
    }


    // ==========================================
    // GUARDAR ORDEN DE LABORATORIO
    // CU-08 FA01
    // CU-09 PRECIOS Y MONTO TOTAL
    // ==========================================

    @PostMapping("/medico/laboratorio/{id}/guardar")
    @Transactional
    public String guardarOrden(
            @PathVariable Long id,

            @RequestParam(
                    name = "examenesIds",
                    required = false
            )
            List<Long> examenesIds,

            @RequestParam(
                    name = "observaciones",
                    required = false,
                    defaultValue = ""
            )
            String observaciones,

            HttpSession session,

            RedirectAttributes redirectAttributes) {

        // ======================================
        // 1. VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {
            return "redirect:/medico/login";
        }

        // ======================================
        // 2. BUSCAR CITA
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
        // 3. VALIDAR MÉDICO
        // ======================================

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(cita, nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para generar órdenes de esta cita."
            );

            return "redirect:/medico";
        }

        // ======================================
        // 4. VALIDAR ESTADO DE LA CITA
        // ======================================

        if (!"EVALUADO".equalsIgnoreCase(cita.getEstado())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita debe estar evaluada para generar una orden de laboratorio."
            );

            return "redirect:/medico";
        }

        // ======================================
        // 5. VALIDAR EXÁMENES SELECCIONADOS
        // ======================================

        if (examenesIds == null || examenesIds.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debe seleccionar al menos un examen de laboratorio."
            );

            return "redirect:/medico/laboratorio/" + id;
        }

        // Eliminar identificadores repetidos

        Set<Long> idsUnicos =
                new LinkedHashSet<>(examenesIds);

        // Consultar exámenes en PostgreSQL

        List<CatalogoExamen> examenes =
                catalogoExamenRepository.findAllById(idsUnicos);

        // ======================================
        // 6. VALIDAR QUE EXISTAN Y ESTÉN ACTIVOS
        // ======================================

        if (examenes.size() != idsUnicos.size()
                || examenes.stream().anyMatch(
                examen -> !Boolean.TRUE.equals(
                        examen.getActivo()
                )
        )) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Uno o varios exámenes seleccionados no existen o están inactivos."
            );

            return "redirect:/medico/laboratorio/" + id;
        }

        // ======================================
        // 7. VALIDAR PRECIOS DEL CATÁLOGO
        // ======================================

        boolean preciosInvalidos = examenes.stream()
                .anyMatch(examen ->
                        examen.getPrecio() == null
                                || examen.getPrecio()
                                .compareTo(BigDecimal.ZERO) < 0
                );

        if (preciosInvalidos) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Uno o varios exámenes no tienen un precio válido. Consulte con administración."
            );

            return "redirect:/medico/laboratorio/" + id;
        }

        // ======================================
        // 8. CREAR ORDEN DE LABORATORIO
        // ======================================

        OrdenLaboratorio orden =
                new OrdenLaboratorio();

        orden.setCita(cita);

        orden.setObservaciones(
                observaciones == null
                        ? ""
                        : observaciones.trim()
        );

        // Estado inicial de la orden
        orden.setEstado("PENDIENTE");

        // Orden interna por defecto
        orden.setOrdenExterna(false);

        // Sin pago registrado
        orden.setFechaPago(null);

        // Sin fecha de finalización
        orden.setFechaCompletada(null);

        // ======================================
        // 9. AGREGAR EXÁMENES Y CALCULAR PRECIOS
        // ======================================

        List<OrdenExamen> detalles =
                new ArrayList<>();

        BigDecimal montoTotal = BigDecimal.ZERO;

        for (CatalogoExamen examen : examenes) {

            // Crear detalle
            OrdenExamen detalle =
                    new OrdenExamen();

            // Asociar con la orden
            detalle.setOrden(orden);

            // Asociar con el catálogo
            detalle.setExamen(examen);

            // Obtener precio del catálogo
            BigDecimal precio =
                    examen.getPrecio();

            // Guardar precio individual
            detalle.setPrecio(precio);

            // Acumular monto total
            montoTotal = montoTotal.add(precio);

            // Inicializar resultado pendiente
            detalle.setValorResultado(null);

            detalle.setUnidad(null);

            detalle.setFechaResultado(null);

            detalle.setFueraRango(false);

            detalle.setNotasResultado(null);

            detalle.setPublicado(false);

            detalle.setFechaPublicacion(null);

            // Agregar a la lista
            detalles.add(detalle);
        }

        // ======================================
        // 10. ASOCIAR EXÁMENES A LA ORDEN
        // ======================================

        orden.setExamenes(detalles);

        // ======================================
        // 11. GUARDAR MONTO TOTAL
        // ======================================

        orden.setMontoTotal(montoTotal);

        // ======================================
        // 12. GUARDAR ORDEN Y EXÁMENES
        // ======================================

        OrdenLaboratorio ordenGuardada =
                ordenLaboratorioRepository.saveAndFlush(orden);

        // ======================================
        // 13. PREPARAR MENSAJE DE CONFIRMACIÓN
        // ======================================

        String nombresExamenes =
                examenes.stream()
                        .map(CatalogoExamen::getNombre)
                        .collect(Collectors.joining(", "));

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Orden de laboratorio #"
                        + ordenGuardada.getId()
                        + " registrada correctamente. Exámenes: "
                        + nombresExamenes
                        + ". Total: Q"
                        + montoTotal.toPlainString()
                        + ". Pago pendiente."
        );

        // ======================================
        // 14. REGRESAR AL PANEL MÉDICO
        // ======================================

        return "redirect:/medico";
    }
}