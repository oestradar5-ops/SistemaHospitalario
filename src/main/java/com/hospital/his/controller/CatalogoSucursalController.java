package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Sucursal;
import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.SucursalRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/catalogos/sucursales")
public class CatalogoSucursalController {

    private final SucursalRepository sucursalRepository;
    private final AuditoriaUsuarioRepository auditoriaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoSucursalController(
            SucursalRepository sucursalRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.sucursalRepository = sucursalRepository;
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
    // LISTAR SUCURSALES
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
                "sucursales",
                sucursalRepository.findAllByOrderByNombreAsc()
        );

        return "admin/catalogos/sucursales/index";
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

        model.addAttribute("sucursal", new Sucursal());

        return "admin/catalogos/sucursales/form";
    }

    // ==========================================
    // REGISTRAR SUCURSAL
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String telefono,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        nombre = limpiar(nombre);
        direccion = limpiar(direccion);
        telefono = limpiar(telefono);

        String error = validarDatos(nombre, direccion, telefono);

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/catalogos/sucursales/create";
        }

        if (sucursalRepository.existsByNombreIgnoreCase(nombre)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una sucursal con ese nombre."
            );

            return "redirect:/admin/catalogos/sucursales/create";
        }

        Sucursal sucursal = new Sucursal();

        sucursal.setNombre(nombre);
        sucursal.setDireccion(direccion.isBlank() ? null : direccion);
        sucursal.setTelefono(telefono.isBlank() ? null : telefono);
        sucursal.setActivo(true);

        sucursalRepository.save(sucursal);

        registrarAuditoria(
                session,
                "CREAR_SUCURSAL",
                "Registró la sucursal " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Sucursal registrada correctamente."
        );

        return "redirect:/admin/catalogos/sucursales";
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

        Sucursal sucursal = sucursalRepository
                .findById(id)
                .orElse(null);

        if (sucursal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal no existe."
            );

            return "redirect:/admin/catalogos/sucursales";
        }

        model.addAttribute("sucursal", sucursal);

        return "admin/catalogos/sucursales/form";
    }

    // ==========================================
    // ACTUALIZAR SUCURSAL
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String telefono,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        Sucursal sucursal = sucursalRepository
                .findById(id)
                .orElse(null);

        if (sucursal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal no existe."
            );

            return "redirect:/admin/catalogos/sucursales";
        }

        nombre = limpiar(nombre);
        direccion = limpiar(direccion);
        telefono = limpiar(telefono);

        String error = validarDatos(nombre, direccion, telefono);

        if (error != null) {

            redirectAttributes.addFlashAttribute("error", error);

            return "redirect:/admin/catalogos/sucursales/edit/" + id;
        }

        if (sucursalRepository.existsByNombreIgnoreCaseAndIdNot(
                nombre, id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe otra sucursal con ese nombre."
            );

            return "redirect:/admin/catalogos/sucursales/edit/" + id;
        }

        sucursal.setNombre(nombre);
        sucursal.setDireccion(direccion.isBlank() ? null : direccion);
        sucursal.setTelefono(telefono.isBlank() ? null : telefono);

        sucursalRepository.save(sucursal);

        registrarAuditoria(
                session,
                "EDITAR_SUCURSAL",
                "Actualizó la sucursal " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Sucursal actualizada correctamente."
        );

        return "redirect:/admin/catalogos/sucursales";
    }

    // ==========================================
    // DESACTIVAR SUCURSAL
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

        Sucursal sucursal = sucursalRepository
                .findById(id)
                .orElse(null);

        if (sucursal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal no existe."
            );

            return "redirect:/admin/catalogos/sucursales";
        }

        if (!Boolean.TRUE.equals(sucursal.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal ya está inactiva."
            );

            return "redirect:/admin/catalogos/sucursales";
        }

        sucursal.setActivo(false);

        sucursalRepository.save(sucursal);

        registrarAuditoria(
                session,
                "DESACTIVAR_SUCURSAL",
                "Desactivó la sucursal " + sucursal.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Sucursal desactivada correctamente."
        );

        return "redirect:/admin/catalogos/sucursales";
    }

    // ==========================================
    // REACTIVAR SUCURSAL
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

        Sucursal sucursal = sucursalRepository
                .findById(id)
                .orElse(null);

        if (sucursal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal no existe."
            );

            return "redirect:/admin/catalogos/sucursales";
        }

        if (Boolean.TRUE.equals(sucursal.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal ya está activa."
            );

            return "redirect:/admin/catalogos/sucursales";
        }

        sucursal.setActivo(true);

        sucursalRepository.save(sucursal);

        registrarAuditoria(
                session,
                "REACTIVAR_SUCURSAL",
                "Reactivó la sucursal " + sucursal.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Sucursal reactivada correctamente."
        );

        return "redirect:/admin/catalogos/sucursales";
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
            String direccion,
            String telefono) {

        if (nombre.isBlank()) {
            return "El nombre de la sucursal es obligatorio.";
        }

        if (nombre.length() > 120) {
            return "El nombre no puede superar los 120 caracteres.";
        }

        if (direccion.length() > 200) {
            return "La dirección no puede superar los 200 caracteres.";
        }

        if (!telefono.isBlank() && !telefono.matches("[0-9]{8}")) {
            return "El teléfono debe contener exactamente 8 dígitos.";
        }

        return null;
    }
}