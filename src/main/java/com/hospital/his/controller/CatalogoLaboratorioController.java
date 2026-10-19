package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Laboratorio;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.LaboratorioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/catalogos/laboratorios")
public class CatalogoLaboratorioController {

    private final LaboratorioRepository laboratorioRepository;
    private final AuditoriaUsuarioRepository auditoriaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoLaboratorioController(
            LaboratorioRepository laboratorioRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

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

        String administrador =
                (String) session.getAttribute("usuario");

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                administrador,
                accion,
                null,
                descripcion
        );

        auditoriaRepository.save(auditoria);
    }

    // ==========================================
    // LISTAR LABORATORIOS
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
                "laboratorios",
                laboratorioRepository.findAllByOrderByNombreAsc()
        );

        return "admin/catalogos/laboratorios/index";
    }

    // ==========================================
    // MOSTRAR FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/create")
    public String mostrarRegistro(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("laboratorio", new Laboratorio());

        return "admin/catalogos/laboratorios/form";
    }

    // ==========================================
    // REGISTRAR LABORATORIO
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String telefono,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);
        ubicacion = limpiar(ubicacion);
        telefono = limpiar(telefono);

        String error = validarDatos(
                nombre,
                ubicacion,
                telefono
        );

        if (error != null) {

            redirectAttributes.addFlashAttribute("error", error);

            return "redirect:/admin/catalogos/laboratorios/create";
        }

        if (laboratorioRepository.existsByNombreIgnoreCase(nombre)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe un laboratorio registrado con ese nombre."
            );

            return "redirect:/admin/catalogos/laboratorios/create";
        }

        Laboratorio laboratorio = new Laboratorio();

        laboratorio.setNombre(nombre);
        laboratorio.setDescripcion(descripcion);
        laboratorio.setUbicacion(ubicacion);
        laboratorio.setTelefono(
                telefono.isBlank() ? null : telefono
        );
        laboratorio.setActivo(true);

        laboratorioRepository.save(laboratorio);

        registrarAuditoria(
                session,
                "CREAR_LABORATORIO",
                "Registró el laboratorio: " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Laboratorio registrado correctamente."
        );

        return "redirect:/admin/catalogos/laboratorios";
    }

    // ==========================================
    // MOSTRAR FORMULARIO DE EDICIÓN
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

        Laboratorio laboratorio = laboratorioRepository
                .findById(id)
                .orElse(null);

        if (laboratorio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El laboratorio solicitado no existe."
            );

            return "redirect:/admin/catalogos/laboratorios";
        }

        model.addAttribute("laboratorio", laboratorio);

        return "admin/catalogos/laboratorios/form";
    }

    // ==========================================
    // ACTUALIZAR LABORATORIO
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String telefono,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        Laboratorio laboratorio = laboratorioRepository
                .findById(id)
                .orElse(null);

        if (laboratorio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El laboratorio solicitado no existe."
            );

            return "redirect:/admin/catalogos/laboratorios";
        }

        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);
        ubicacion = limpiar(ubicacion);
        telefono = limpiar(telefono);

        String error = validarDatos(
                nombre,
                ubicacion,
                telefono
        );

        if (error != null) {

            redirectAttributes.addFlashAttribute("error", error);

            return "redirect:/admin/catalogos/laboratorios/edit/" + id;
        }

        if (laboratorioRepository.existsByNombreIgnoreCaseAndIdNot(
                nombre, id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe otro laboratorio con ese nombre."
            );

            return "redirect:/admin/catalogos/laboratorios/edit/" + id;
        }

        laboratorio.setNombre(nombre);
        laboratorio.setDescripcion(descripcion);
        laboratorio.setUbicacion(ubicacion);
        laboratorio.setTelefono(
                telefono.isBlank() ? null : telefono
        );

        laboratorioRepository.save(laboratorio);

        registrarAuditoria(
                session,
                "EDITAR_LABORATORIO",
                "Actualizó el laboratorio ID " + id
                        + ": " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Laboratorio actualizado correctamente."
        );

        return "redirect:/admin/catalogos/laboratorios";
    }

    // ==========================================
    // DESACTIVAR LABORATORIO
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

        Laboratorio laboratorio = laboratorioRepository
                .findById(id)
                .orElse(null);

        if (laboratorio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El laboratorio solicitado no existe."
            );

            return "redirect:/admin/catalogos/laboratorios";
        }

        if (!Boolean.TRUE.equals(laboratorio.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El laboratorio ya se encuentra inactivo."
            );

            return "redirect:/admin/catalogos/laboratorios";
        }

        laboratorio.setActivo(false);

        laboratorioRepository.save(laboratorio);

        registrarAuditoria(
                session,
                "DESACTIVAR_LABORATORIO",
                "Desactivó el laboratorio ID " + id
                        + ": " + laboratorio.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Laboratorio desactivado correctamente."
        );

        return "redirect:/admin/catalogos/laboratorios";
    }

    // ==========================================
    // REACTIVAR LABORATORIO
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

        Laboratorio laboratorio = laboratorioRepository
                .findById(id)
                .orElse(null);

        if (laboratorio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El laboratorio solicitado no existe."
            );

            return "redirect:/admin/catalogos/laboratorios";
        }

        if (Boolean.TRUE.equals(laboratorio.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El laboratorio ya se encuentra activo."
            );

            return "redirect:/admin/catalogos/laboratorios";
        }

        laboratorio.setActivo(true);

        laboratorioRepository.save(laboratorio);

        registrarAuditoria(
                session,
                "REACTIVAR_LABORATORIO",
                "Reactivó el laboratorio ID " + id
                        + ": " + laboratorio.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Laboratorio reactivado correctamente."
        );

        return "redirect:/admin/catalogos/laboratorios";
    }

    // ==========================================
    // LIMPIAR TEXO
    // ==========================================

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // ==========================================
    // VALIDAR DATOS
    // ==========================================

    private String validarDatos(
            String nombre,
            String ubicacion,
            String telefono) {

        if (nombre.isBlank()) {
            return "El nombre del laboratorio es obligatorio.";
        }

        if (nombre.length() > 120) {
            return "El nombre no puede superar los 120 caracteres.";
        }

        if (ubicacion.length() > 200) {
            return "La ubicación no puede superar los 200 caracteres.";
        }

        if (!telefono.isBlank()
                && !telefono.matches("[0-9]{8}")) {

            return "El teléfono debe contener exactamente 8 dígitos.";
        }

        return null;
    }
}