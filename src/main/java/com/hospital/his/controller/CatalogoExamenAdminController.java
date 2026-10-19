package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.CatalogoExamen;
import com.hospital.his.entity.Laboratorio;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.CatalogoExamenRepository;
import com.hospital.his.repository.LaboratorioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/catalogos/examenes")
public class CatalogoExamenAdminController {

    private final CatalogoExamenRepository examenRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final AuditoriaUsuarioRepository auditoriaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoExamenAdminController(
            CatalogoExamenRepository examenRepository,
            LaboratorioRepository laboratorioRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.examenRepository = examenRepository;
        this.laboratorioRepository = laboratorioRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private boolean esAdministrador(HttpSession session) {

        Object usuario = session.getAttribute("usuario");
        Object rol = session.getAttribute("rol");

        return usuario instanceof String
                && !((String) usuario).isBlank()
                && rol instanceof String
                && (
                "ADMIN".equalsIgnoreCase((String) rol)
                        || "ADMINISTRADOR".equalsIgnoreCase((String) rol)
        );
    }

    // ==========================================
    // REGISTRAR AUDITORÍA
    // ==========================================

    private void registrarAuditoria(
            HttpSession session,
            String accion,
            String descripcion) {

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                (String) session.getAttribute("usuario"),
                accion,
                null,
                descripcion
        );

        auditoriaRepository.save(auditoria);
    }
// ==========================================
// LISTAR EXÁMENES
// CU-14
// ==========================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listar(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "examenes",
                examenRepository.findAllConLaboratorio()
        );

        return "admin/catalogos/examenes/index";
    }

    // ==========================================
    // FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/create")
    @Transactional(readOnly = true)
    public String mostrarRegistro(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("examen", new CatalogoExamen());

        model.addAttribute(
                "laboratorios",
                laboratorioRepository.findByActivoTrueOrderByNombreAsc()
        );

        return "admin/catalogos/examenes/form";
    }

    // ==========================================
    // REGISTRAR EXAMEN
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String precio,
            @RequestParam(required = false) String rangoReferencia,
            @RequestParam(required = false) String unidadReferencia,
            @RequestParam(required = false) Long laboratorioId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);
        rangoReferencia = limpiar(rangoReferencia);
        unidadReferencia = limpiar(unidadReferencia);

        String error = validarDatos(
                nombre,
                rangoReferencia,
                unidadReferencia
        );

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/catalogos/examenes/create";
        }

        BigDecimal precioValidado = convertirPrecio(precio);

        if (precioValidado == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "El precio debe ser un número mayor que cero, con máximo dos decimales."
            );

            return "redirect:/admin/catalogos/examenes/create";
        }

        if (examenRepository.existsByNombreIgnoreCase(nombre)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe un examen registrado con ese nombre."
            );

            return "redirect:/admin/catalogos/examenes/create";
        }

        Laboratorio laboratorio = buscarLaboratorioActivo(laboratorioId);

        if (laboratorio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debes seleccionar un laboratorio activo."
            );

            return "redirect:/admin/catalogos/examenes/create";
        }

        CatalogoExamen examen = new CatalogoExamen();

        examen.setNombre(nombre);
        examen.setDescripcion(descripcion);
        examen.setPrecio(precioValidado);
        examen.setRangoReferencia(rangoReferencia);
        examen.setUnidadReferencia(unidadReferencia);
        examen.setLaboratorio(laboratorio);
        examen.setActivo(true);

        examenRepository.save(examen);

        registrarAuditoria(
                session,
                "CREAR_EXAMEN",
                "Registró el examen: " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Examen registrado correctamente."
        );

        return "redirect:/admin/catalogos/examenes";
    }

    // ==========================================
    // FORMULARIO DE EDICIÓN
    // ==========================================

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String mostrarEdicion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        CatalogoExamen examen = examenRepository
                .findById(id)
                .orElse(null);

        if (examen == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El examen solicitado no existe."
            );

            return "redirect:/admin/catalogos/examenes";
        }

        model.addAttribute("examen", examen);

        model.addAttribute(
                "laboratorios",
                laboratorioRepository.findByActivoTrueOrderByNombreAsc()
        );

        return "admin/catalogos/examenes/form";
    }

    // ==========================================
    // ACTUALIZAR EXAMEN
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String precio,
            @RequestParam(required = false) String rangoReferencia,
            @RequestParam(required = false) String unidadReferencia,
            @RequestParam(required = false) Long laboratorioId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        CatalogoExamen examen = examenRepository
                .findById(id)
                .orElse(null);

        if (examen == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El examen solicitado no existe."
            );

            return "redirect:/admin/catalogos/examenes";
        }

        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);
        rangoReferencia = limpiar(rangoReferencia);
        unidadReferencia = limpiar(unidadReferencia);

        String error = validarDatos(
                nombre,
                rangoReferencia,
                unidadReferencia
        );

        if (error != null) {

            redirectAttributes.addFlashAttribute("error", error);

            return "redirect:/admin/catalogos/examenes/edit/" + id;
        }

        BigDecimal precioValidado = convertirPrecio(precio);

        if (precioValidado == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El precio debe ser un número mayor que cero, con máximo dos decimales."
            );

            return "redirect:/admin/catalogos/examenes/edit/" + id;
        }

        if (examenRepository.existsByNombreIgnoreCaseAndIdNot(
                nombre, id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe otro examen con ese nombre."
            );

            return "redirect:/admin/catalogos/examenes/edit/" + id;
        }

        Laboratorio laboratorio = buscarLaboratorioActivo(laboratorioId);

        if (laboratorio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debes seleccionar un laboratorio activo."
            );

            return "redirect:/admin/catalogos/examenes/edit/" + id;
        }

        examen.setNombre(nombre);
        examen.setDescripcion(descripcion);
        examen.setPrecio(precioValidado);
        examen.setRangoReferencia(rangoReferencia);
        examen.setUnidadReferencia(unidadReferencia);
        examen.setLaboratorio(laboratorio);

        examenRepository.save(examen);

        registrarAuditoria(
                session,
                "EDITAR_EXAMEN",
                "Actualizó el examen ID " + id + ": " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Examen actualizado correctamente."
        );

        return "redirect:/admin/catalogos/examenes";
    }

    // ==========================================
    // DESACTIVAR EXAMEN
    // ==========================================

    @PostMapping("/delete/{id}")
    @Transactional
    public String desactivar(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        CatalogoExamen examen = examenRepository
                .findById(id)
                .orElse(null);

        if (examen == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El examen solicitado no existe."
            );

            return "redirect:/admin/catalogos/examenes";
        }

        if (!Boolean.TRUE.equals(examen.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El examen ya está inactivo."
            );

            return "redirect:/admin/catalogos/examenes";
        }

        examen.setActivo(false);

        examenRepository.save(examen);

        registrarAuditoria(
                session,
                "DESACTIVAR_EXAMEN",
                "Desactivó el examen ID " + id
                        + ": " + examen.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Examen desactivado correctamente."
        );

        return "redirect:/admin/catalogos/examenes";
    }

    // ==========================================
    // REACTIVAR EXAMEN
    // ==========================================

    @PostMapping("/activate/{id}")
    @Transactional
    public String reactivar(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        CatalogoExamen examen = examenRepository
                .findById(id)
                .orElse(null);

        if (examen == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El examen solicitado no existe."
            );

            return "redirect:/admin/catalogos/examenes";
        }

        if (Boolean.TRUE.equals(examen.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El examen ya está activo."
            );

            return "redirect:/admin/catalogos/examenes";
        }

        examen.setActivo(true);

        examenRepository.save(examen);

        registrarAuditoria(
                session,
                "REACTIVAR_EXAMEN",
                "Reactivó el examen ID " + id
                        + ": " + examen.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Examen reactivado correctamente."
        );

        return "redirect:/admin/catalogos/examenes";
    }

    // ==========================================
    // BUSCAR LABORATORIO ACTIVO
    // ==========================================

    private Laboratorio buscarLaboratorioActivo(Long laboratorioId) {

        if (laboratorioId == null) {
            return null;
        }

        Laboratorio laboratorio = laboratorioRepository
                .findById(laboratorioId)
                .orElse(null);

        if (laboratorio == null
                || !Boolean.TRUE.equals(laboratorio.getActivo())) {
            return null;
        }

        return laboratorio;
    }

    // ==========================================
    // LIMPIAR TEXTO
    // ==========================================

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // ==========================================
    // VALIDAR DATOS
    // ==========================================

    private String validarDatos(
            String nombre,
            String rangoReferencia,
            String unidadReferencia) {

        if (nombre.isBlank()) {
            return "El nombre del examen es obligatorio.";
        }

        if (nombre.length() > 150) {
            return "El nombre no puede superar los 150 caracteres.";
        }

        if (rangoReferencia.length() > 150) {
            return "El rango de referencia no puede superar los 150 caracteres.";
        }

        if (unidadReferencia.length() > 50) {
            return "La unidad de referencia no puede superar los 50 caracteres.";
        }

        return null;
    }

    // ==========================================
    // VALIDAR Y CONVERTIR PRECIO
    // ==========================================

    private BigDecimal convertirPrecio(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {

            BigDecimal precio = new BigDecimal(valor.trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            if (precio.scale() > 2) {
                return null;
            }

            if (precio.precision() - precio.scale() > 8) {
                return null;
            }

            return precio;

        } catch (NumberFormatException e) {
            return null;
        }
    }
}