package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.EstadoCita;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.EstadoCitaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/admin/catalogos/estados-cita")
public class CatalogoEstadoCitaController {

    private final EstadoCitaRepository estadoRepository;
    private final AuditoriaUsuarioRepository auditoriaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoEstadoCitaController(
            EstadoCitaRepository estadoRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.estadoRepository = estadoRepository;
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

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                (String) session.getAttribute("usuario"),
                accion,
                null,
                descripcion
        );

        auditoriaRepository.save(auditoria);
    }

    // ==========================================
    // LISTAR ESTADOS
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
                "estados",
                estadoRepository.findAllByOrderByNombreAsc()
        );

        return "admin/catalogos/estados-cita/index";
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

        model.addAttribute("estado", new EstadoCita());

        return "admin/catalogos/estados-cita/form";
    }

    // ==========================================
    // REGISTRAR NUEVO ESTADO
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        codigo = normalizarCodigo(codigo);
        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);

        String error = validarDatos(codigo, nombre);

        if (error != null) {

            redirectAttributes.addFlashAttribute("error", error);

            return "redirect:/admin/catalogos/estados-cita/create";
        }

        if (estadoRepository.existsByCodigoIgnoreCase(codigo)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe un estado con ese código."
            );

            return "redirect:/admin/catalogos/estados-cita/create";
        }

        EstadoCita estado = new EstadoCita();

        estado.setCodigo(codigo);
        estado.setNombre(nombre);
        estado.setDescripcion(descripcion);
        estado.setActivo(true);
        estado.setProtegido(false);

        estadoRepository.save(estado);

        registrarAuditoria(
                session,
                "CREAR_ESTADO_CITA",
                "Registró el estado de cita " + codigo
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Estado de cita registrado correctamente."
        );

        return "redirect:/admin/catalogos/estados-cita";
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

        EstadoCita estado = estadoRepository
                .findById(id)
                .orElse(null);

        if (estado == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado de cita no existe."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        model.addAttribute("estado", estado);

        return "admin/catalogos/estados-cita/form";
    }

    // ==========================================
    // ACTUALIZAR ESTADO
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        EstadoCita estado = estadoRepository
                .findById(id)
                .orElse(null);

        if (estado == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado de cita no existe."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        codigo = normalizarCodigo(codigo);
        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);

        // ==========================================
        // PROTEGER CÓDIGOS DEL SISTEMA
        // ==========================================

        if (Boolean.TRUE.equals(estado.getProtegido())) {

            if (!estado.getCodigo().equals(codigo)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "No se puede modificar el código de un estado protegido."
                );

                return "redirect:/admin/catalogos/estados-cita/edit/" + id;
            }

        } else {

            String error = validarDatos(codigo, nombre);

            if (error != null) {

                redirectAttributes.addFlashAttribute("error", error);

                return "redirect:/admin/catalogos/estados-cita/edit/" + id;
            }

            if (estadoRepository.existsByCodigoIgnoreCaseAndIdNot(
                    codigo, id)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Ya existe otro estado con ese código."
                );

                return "redirect:/admin/catalogos/estados-cita/edit/" + id;
            }

            estado.setCodigo(codigo);
        }

        if (nombre.isBlank() || nombre.length() > 100) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre es obligatorio y no puede superar los 100 caracteres."
            );

            return "redirect:/admin/catalogos/estados-cita/edit/" + id;
        }

        estado.setNombre(nombre);
        estado.setDescripcion(descripcion);

        estadoRepository.save(estado);

        registrarAuditoria(
                session,
                "EDITAR_ESTADO_CITA",
                "Actualizó el estado de cita " + estado.getCodigo()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Estado de cita actualizado correctamente."
        );

        return "redirect:/admin/catalogos/estados-cita";
    }

    // ==========================================
    // DESACTIVAR ESTADO
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

        EstadoCita estado = estadoRepository
                .findById(id)
                .orElse(null);

        if (estado == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado de cita no existe."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        if (Boolean.TRUE.equals(estado.getProtegido())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede desactivar un estado protegido del sistema."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        if (!Boolean.TRUE.equals(estado.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado ya está inactivo."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        estado.setActivo(false);

        estadoRepository.save(estado);

        registrarAuditoria(
                session,
                "DESACTIVAR_ESTADO_CITA",
                "Desactivó el estado " + estado.getCodigo()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Estado de cita desactivado correctamente."
        );

        return "redirect:/admin/catalogos/estados-cita";
    }

    // ==========================================
    // REACTIVAR ESTADO
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

        EstadoCita estado = estadoRepository
                .findById(id)
                .orElse(null);

        if (estado == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado de cita no existe."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        if (Boolean.TRUE.equals(estado.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado ya está activo."
            );

            return "redirect:/admin/catalogos/estados-cita";
        }

        estado.setActivo(true);

        estadoRepository.save(estado);

        registrarAuditoria(
                session,
                "REACTIVAR_ESTADO_CITA",
                "Reactivó el estado " + estado.getCodigo()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Estado de cita reactivado correctamente."
        );

        return "redirect:/admin/catalogos/estados-cita";
    }

    // ==========================================
    // LIMPIAR TEXTO
    // ==========================================

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // ==========================================
    // NORMALIZAR CÓDIGO
    // ==========================================

    private String normalizarCodigo(String codigo) {

        return limpiar(codigo)
                .toUpperCase(Locale.ROOT);
    }

    // ==========================================
    // VALIDAR DATOS
    // ==========================================

    private String validarDatos(
            String codigo,
            String nombre) {

        if (codigo.isBlank()) {
            return "El código del estado es obligatorio.";
        }

        if (codigo.length() > 50) {
            return "El código no puede superar los 50 caracteres.";
        }

        if (!codigo.matches("[A-Z0-9_]+")) {
            return "El código solo puede contener letras, números y guiones bajos.";
        }

        if (nombre.isBlank()) {
            return "El nombre del estado es obligatorio.";
        }

        if (nombre.length() > 100) {
            return "El nombre no puede superar los 100 caracteres.";
        }

        return null;
    }
}