package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Especialidad;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.EspecialidadRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/catalogos/especialidades")
public class CatalogoEspecialidadController {

    private final EspecialidadRepository especialidadRepository;
    private final AuditoriaUsuarioRepository auditoriaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoEspecialidadController(
            EspecialidadRepository especialidadRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.especialidadRepository = especialidadRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private boolean esAdministrador(HttpSession session) {

        String usuario = (String) session.getAttribute("usuario");
        String rol = (String) session.getAttribute("rol");

        return usuario != null
                && !usuario.isBlank()
                && rol != null
                && (
                "ADMIN".equalsIgnoreCase(rol)
                        || "ADMINISTRADOR".equalsIgnoreCase(rol)
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
    // LISTAR ESPECIALIDADES
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
                "especialidades",
                especialidadRepository.findAllByOrderByNombreAsc()
        );

        return "admin/catalogos/especialidades/index";
    }

    // ==========================================
    // MOSTRAR FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/create")
    public String mostrarFormulario(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "especialidad",
                new Especialidad()
        );

        return "admin/catalogos/especialidades/form";
    }

    // ==========================================
    // REGISTRAR ESPECIALIDAD
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        nombre = nombre == null ? "" : nombre.trim();
        descripcion = descripcion == null ? "" : descripcion.trim();

        if (nombre.isBlank() || descripcion.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre y la descripción son obligatorios."
            );

            return "redirect:/admin/catalogos/especialidades/create";
        }

        if (nombre.length() > 120) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre no puede superar los 120 caracteres."
            );

            return "redirect:/admin/catalogos/especialidades/create";
        }

        if (especialidadRepository.existsByNombreIgnoreCase(nombre)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una especialidad con ese nombre."
            );

            return "redirect:/admin/catalogos/especialidades/create";
        }

        Especialidad especialidad = new Especialidad();

        especialidad.setNombre(nombre);
        especialidad.setDescripcion(descripcion);
        especialidad.setActivo(true);

        especialidadRepository.save(especialidad);

        registrarAuditoria(
                session,
                "CREAR_ESPECIALIDAD",
                "Registró la especialidad " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Especialidad registrada correctamente."
        );

        return "redirect:/admin/catalogos/especialidades";
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

        Especialidad especialidad = especialidadRepository
                .findById(id)
                .orElse(null);

        if (especialidad == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La especialidad no existe."
            );

            return "redirect:/admin/catalogos/especialidades";
        }

        model.addAttribute("especialidad", especialidad);

        return "admin/catalogos/especialidades/form";
    }

    // ==========================================
    // ACTUALIZAR ESPECIALIDAD
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        Especialidad especialidad = especialidadRepository
                .findById(id)
                .orElse(null);

        if (especialidad == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La especialidad no existe."
            );

            return "redirect:/admin/catalogos/especialidades";
        }

        nombre = nombre == null ? "" : nombre.trim();
        descripcion = descripcion == null ? "" : descripcion.trim();

        if (nombre.isBlank() || descripcion.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre y la descripción son obligatorios."
            );

            return "redirect:/admin/catalogos/especialidades/edit/" + id;
        }

        if (nombre.length() > 120) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre no puede superar los 120 caracteres."
            );

            return "redirect:/admin/catalogos/especialidades/edit/" + id;
        }

        if (especialidadRepository.existsByNombreIgnoreCaseAndIdNot(
                nombre,
                id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe otra especialidad con ese nombre."
            );

            return "redirect:/admin/catalogos/especialidades/edit/" + id;
        }

        especialidad.setNombre(nombre);
        especialidad.setDescripcion(descripcion);

        especialidadRepository.save(especialidad);

        registrarAuditoria(
                session,
                "EDITAR_ESPECIALIDAD",
                "Actualizó la especialidad " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Especialidad actualizada correctamente."
        );

        return "redirect:/admin/catalogos/especialidades";
    }

    // ==========================================
    // DESACTIVAR ESPECIALIDAD
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

        Especialidad especialidad = especialidadRepository
                .findById(id)
                .orElse(null);

        if (especialidad == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La especialidad no existe."
            );

            return "redirect:/admin/catalogos/especialidades";
        }

        if (!Boolean.TRUE.equals(especialidad.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La especialidad ya está inactiva."
            );

            return "redirect:/admin/catalogos/especialidades";
        }

        especialidad.setActivo(false);

        especialidadRepository.save(especialidad);

        registrarAuditoria(
                session,
                "DESACTIVAR_ESPECIALIDAD",
                "Desactivó la especialidad "
                        + especialidad.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Especialidad desactivada correctamente."
        );

        return "redirect:/admin/catalogos/especialidades";
    }

    // ==========================================
    // REACTIVAR ESPECIALIDAD
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

        Especialidad especialidad = especialidadRepository
                .findById(id)
                .orElse(null);

        if (especialidad == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La especialidad no existe."
            );

            return "redirect:/admin/catalogos/especialidades";
        }

        if (Boolean.TRUE.equals(especialidad.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La especialidad ya está activa."
            );

            return "redirect:/admin/catalogos/especialidades";
        }

        especialidad.setActivo(true);

        especialidadRepository.save(especialidad);

        registrarAuditoria(
                session,
                "REACTIVAR_ESPECIALIDAD",
                "Reactivó la especialidad "
                        + especialidad.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Especialidad reactivada correctamente."
        );

        return "redirect:/admin/catalogos/especialidades";
    }
}