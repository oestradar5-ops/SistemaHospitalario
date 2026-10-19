package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.CatalogoMedicamento;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.CatalogoMedicamentoRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/catalogos/medicamentos")
public class CatalogoMedicamentoAdminController {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final CatalogoMedicamentoRepository medicamentoRepository;

    private final AuditoriaUsuarioRepository auditoriaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoMedicamentoAdminController(
            CatalogoMedicamentoRepository medicamentoRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.medicamentoRepository = medicamentoRepository;
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
    // LISTAR MEDICAMENTOS
    // ==========================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listar(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        List<CatalogoMedicamento> medicamentos =
                medicamentoRepository.findAllByOrderByNombreAsc();

        model.addAttribute("medicamentos", medicamentos);

        return "admin/catalogos/medicamentos/index";
    }


    // ==========================================
    // FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/create")
    public String mostrarRegistro(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "medicamento",
                new CatalogoMedicamento()
        );

        return "admin/catalogos/medicamentos/form";
    }


    // ==========================================
    // REGISTRAR MEDICAMENTO
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String presentacion,
            @RequestParam(required = false) String concentracion,
            @RequestParam(required = false) String unidadMedida,
            @RequestParam(defaultValue = "false") Boolean controlado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        nombre = limpiar(nombre);
        presentacion = limpiar(presentacion);
        concentracion = limpiar(concentracion);
        unidadMedida = limpiar(unidadMedida);

        // VALIDAR DATOS

        String error = validarDatos(
                nombre,
                presentacion,
                concentracion,
                unidadMedida
        );

        if (error != null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    error
            );

            return "redirect:/admin/catalogos/medicamentos/create";
        }

        // VALIDAR DUPLICADOS

        if (existeDuplicado(
                nombre,
                presentacion,
                concentracion,
                null)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe un medicamento con el mismo nombre, presentación y concentración."
            );

            return "redirect:/admin/catalogos/medicamentos/create";
        }

        // CREAR MEDICAMENTO

        CatalogoMedicamento medicamento =
                new CatalogoMedicamento();

        medicamento.setNombre(nombre);
        medicamento.setPresentacion(presentacion);
        medicamento.setConcentracion(concentracion);
        medicamento.setUnidadMedida(unidadMedida);
        medicamento.setControlado(Boolean.TRUE.equals(controlado));
        medicamento.setActivo(true);

        medicamentoRepository.save(medicamento);

        // AUDITORÍA

        registrarAuditoria(
                session,
                "CREAR_MEDICAMENTO",
                "Registró el medicamento: " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Medicamento registrado correctamente."
        );

        return "redirect:/admin/catalogos/medicamentos";
    }


    // ==========================================
    // FORMULARIO DE EDICIÓN
    // ==========================================

    @GetMapping("/edit/{id}")
    public String mostrarEdicion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        CatalogoMedicamento medicamento =
                medicamentoRepository.findById(id).orElse(null);

        if (medicamento == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento solicitado no existe."
            );

            return "redirect:/admin/catalogos/medicamentos";
        }

        model.addAttribute("medicamento", medicamento);

        return "admin/catalogos/medicamentos/form";
    }


    // ==========================================
    // ACTUALIZAR MEDICAMENTO
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizar(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String presentacion,
            @RequestParam(required = false) String concentracion,
            @RequestParam(required = false) String unidadMedida,
            @RequestParam(defaultValue = "false") Boolean controlado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        CatalogoMedicamento medicamento =
                medicamentoRepository.findById(id).orElse(null);

        if (medicamento == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento solicitado no existe."
            );

            return "redirect:/admin/catalogos/medicamentos";
        }

        nombre = limpiar(nombre);
        presentacion = limpiar(presentacion);
        concentracion = limpiar(concentracion);
        unidadMedida = limpiar(unidadMedida);

        // VALIDAR DATOS

        String error = validarDatos(
                nombre,
                presentacion,
                concentracion,
                unidadMedida
        );

        if (error != null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    error
            );

            return "redirect:/admin/catalogos/medicamentos/edit/" + id;
        }

        // VALIDAR DUPLICADOS

        if (existeDuplicado(
                nombre,
                presentacion,
                concentracion,
                id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe otro medicamento con el mismo nombre, presentación y concentración."
            );

            return "redirect:/admin/catalogos/medicamentos/edit/" + id;
        }

        // ACTUALIZAR DATOS

        medicamento.setNombre(nombre);
        medicamento.setPresentacion(presentacion);
        medicamento.setConcentracion(concentracion);
        medicamento.setUnidadMedida(unidadMedida);
        medicamento.setControlado(Boolean.TRUE.equals(controlado));

        medicamentoRepository.save(medicamento);

        // AUDITORÍA

        registrarAuditoria(
                session,
                "EDITAR_MEDICAMENTO",
                "Actualizó el medicamento ID "
                        + id + ": " + nombre
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Medicamento actualizado correctamente."
        );

        return "redirect:/admin/catalogos/medicamentos";
    }


    // ==========================================
    // DESACTIVAR MEDICAMENTO
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

        CatalogoMedicamento medicamento =
                medicamentoRepository.findById(id).orElse(null);

        if (medicamento == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento solicitado no existe."
            );

            return "redirect:/admin/catalogos/medicamentos";
        }

        if (!Boolean.TRUE.equals(medicamento.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento ya está inactivo."
            );

            return "redirect:/admin/catalogos/medicamentos";
        }

        medicamento.setActivo(false);

        medicamentoRepository.save(medicamento);

        registrarAuditoria(
                session,
                "DESACTIVAR_MEDICAMENTO",
                "Desactivó el medicamento ID "
                        + id + ": " + medicamento.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Medicamento desactivado correctamente."
        );

        return "redirect:/admin/catalogos/medicamentos";
    }


    // ==========================================
    // REACTIVAR MEDICAMENTO
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

        CatalogoMedicamento medicamento =
                medicamentoRepository.findById(id).orElse(null);

        if (medicamento == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento solicitado no existe."
            );

            return "redirect:/admin/catalogos/medicamentos";
        }

        if (Boolean.TRUE.equals(medicamento.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento ya está activo."
            );

            return "redirect:/admin/catalogos/medicamentos";
        }

        medicamento.setActivo(true);

        medicamentoRepository.save(medicamento);

        registrarAuditoria(
                session,
                "REACTIVAR_MEDICAMENTO",
                "Reactivó el medicamento ID "
                        + id + ": " + medicamento.getNombre()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Medicamento reactivado correctamente."
        );

        return "redirect:/admin/catalogos/medicamentos";
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
            String presentacion,
            String concentracion,
            String unidadMedida) {

        if (nombre.isBlank()) {
            return "El nombre del medicamento es obligatorio.";
        }

        if (nombre.length() > 150) {
            return "El nombre no puede superar los 150 caracteres.";
        }

        if (presentacion.isBlank()) {
            return "La presentación del medicamento es obligatoria.";
        }

        if (presentacion.length() > 100) {
            return "La presentación no puede superar los 100 caracteres.";
        }

        if (concentracion.isBlank()) {
            return "La concentración del medicamento es obligatoria.";
        }

        if (concentracion.length() > 100) {
            return "La concentración no puede superar los 100 caracteres.";
        }

        if (unidadMedida.isBlank()) {
            return "La unidad de medida es obligatoria.";
        }

        if (unidadMedida.length() > 50) {
            return "La unidad de medida no puede superar los 50 caracteres.";
        }

        return null;
    }


    // ==========================================
    // VALIDAR MEDICAMENTOS DUPLICADOS
    // NOMBRE + PRESENTACIÓN + CONCENTRACIÓN
    // ==========================================

    private boolean existeDuplicado(
            String nombre,
            String presentacion,
            String concentracion,
            Long idExcluir) {

        List<CatalogoMedicamento> medicamentos =
                medicamentoRepository.findAll();

        for (CatalogoMedicamento existente : medicamentos) {

            if (idExcluir != null
                    && idExcluir.equals(existente.getId())) {
                continue;
            }

            boolean mismoNombre =
                    nombre.equalsIgnoreCase(
                            limpiar(existente.getNombre())
                    );

            boolean mismaPresentacion =
                    presentacion.equalsIgnoreCase(
                            limpiar(existente.getPresentacion())
                    );

            boolean mismaConcentracion =
                    concentracion.equalsIgnoreCase(
                            limpiar(existente.getConcentracion())
                    );

            if (mismoNombre
                    && mismaPresentacion
                    && mismaConcentracion) {

                return true;
            }
        }

        return false;
    }
}