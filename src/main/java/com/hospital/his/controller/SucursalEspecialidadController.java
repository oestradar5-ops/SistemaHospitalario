package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Especialidad;
import com.hospital.his.entity.Sucursal;
import com.hospital.his.entity.SucursalEspecialidad;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.EspecialidadRepository;
import com.hospital.his.repository.SucursalEspecialidadRepository;
import com.hospital.his.repository.SucursalRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/branch-specialty")
public class SucursalEspecialidadController {

    private final SucursalEspecialidadRepository asignacionRepository;
    private final SucursalRepository sucursalRepository;
    private final EspecialidadRepository especialidadRepository;
    private final AuditoriaUsuarioRepository auditoriaRepository;

    public SucursalEspecialidadController(
            SucursalEspecialidadRepository asignacionRepository,
            SucursalRepository sucursalRepository,
            EspecialidadRepository especialidadRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.asignacionRepository = asignacionRepository;
        this.sucursalRepository = sucursalRepository;
        this.especialidadRepository = especialidadRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

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
    // LISTADO, FILTRO Y PAGINACIÓN
    // ==========================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listar(
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        int pagina = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
                pagina,
                10,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<SucursalEspecialidad> asignaciones =
                asignacionRepository.buscarAsignaciones(id, pageable);

        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("idBusqueda", id);
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", asignaciones.getTotalPages());

        return "branch-specialty/index";
    }

    // ==========================================
    // MOSTRAR FORMULARIO
    // ==========================================

    @GetMapping("/create")
    public String mostrarFormulario(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        cargarCatalogos(model);

        return "branch-specialty/create";
    }

    // ==========================================
    // REGISTRAR O REACTIVAR ASIGNACIÓN
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String asignar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long especialidadId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // VALIDAR SEDE OBLIGATORIA

        if (sucursalId == null) {

            model.addAttribute(
                    "error",
                    "Debe seleccionar una sede."
            );

            cargarCatalogos(model);

            return "branch-specialty/create";
        }

        // VALIDAR ESPECIALIDAD OBLIGATORIA

        if (especialidadId == null) {

            model.addAttribute(
                    "error",
                    "Debe seleccionar una especialidad."
            );

            cargarCatalogos(model);

            return "branch-specialty/create";
        }

        // BUSCAR SEDE

        Sucursal sucursal = sucursalRepository
                .findById(sucursalId)
                .orElse(null);

        // BUSCAR ESPECIALIDAD

        Especialidad especialidad = especialidadRepository
                .findById(especialidadId)
                .orElse(null);

        // VALIDAR SEDE ACTIVA

        if (sucursal == null
                || !Boolean.TRUE.equals(sucursal.getActivo())) {

            model.addAttribute(
                    "error",
                    "La sede seleccionada no está disponible."
            );

            cargarCatalogos(model);

            return "branch-specialty/create";
        }

        // VALIDAR ESPECIALIDAD ACTIVA

        if (especialidad == null
                || !Boolean.TRUE.equals(especialidad.getActivo())) {

            model.addAttribute(
                    "error",
                    "La especialidad seleccionada no está disponible."
            );

            cargarCatalogos(model);

            return "branch-specialty/create";
        }

        // COMPROBAR ASIGNACIÓN EXISTENTE

        SucursalEspecialidad asignacion = asignacionRepository
                .findBySucursalIdAndEspecialidadId(
                        sucursalId,
                        especialidadId
                )
                .orElse(null);

        // EVITAR DUPLICADOS ACTIVOS

        if (asignacion != null
                && Boolean.TRUE.equals(asignacion.getActivo())) {

            model.addAttribute(
                    "error",
                    "Esta especialidad ya está asignada a la sede."
            );

            cargarCatalogos(model);

            return "branch-specialty/create";
        }

        // CREAR O REACTIVAR

        boolean reactivacion = asignacion != null;

        if (asignacion == null) {
            asignacion = new SucursalEspecialidad();
        }

        asignacion.setSucursal(sucursal);
        asignacion.setEspecialidad(especialidad);
        asignacion.setActivo(true);

        asignacionRepository.save(asignacion);

        // REGISTRAR AUDITORÍA

        String administrador =
                (String) session.getAttribute("usuario");

        String accion = reactivacion
                ? "REACTIVAR_ASIGNACION"
                : "ASIGNAR_ESPECIALIDAD";

        String descripcion = reactivacion
                ? "Reactivó la especialidad "
                + especialidad.getNombre()
                + " en la sede "
                + sucursal.getNombre()
                : "Asignó la especialidad "
                + especialidad.getNombre()
                + " a la sede "
                + sucursal.getNombre();

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                administrador,
                accion,
                null,
                descripcion
        );

        auditoriaRepository.save(auditoria);

        // MENSAJE DE CONFIRMACIÓN

        redirectAttributes.addFlashAttribute(
                "mensaje",
                reactivacion
                        ? "Especialidad reactivada en la sede correctamente."
                        : "Especialidad asignada a la sede correctamente."
        );

        return "redirect:/branch-specialty";
    }

    // ==========================================
    // ELIMINAR ASIGNACIÓN (DESACTIVACIÓN LÓGICA)
    // ==========================================

    @PostMapping("/delete/{id}")
    @Transactional
    public String eliminar(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        SucursalEspecialidad asignacion = asignacionRepository
                .findById(id)
                .orElse(null);

        // VALIDAR EXISTENCIA

        if (asignacion == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La asignación no existe."
            );

            return "redirect:/branch-specialty";
        }

        // EVITAR ELIMINAR DOS VECES

        if (!Boolean.TRUE.equals(asignacion.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Esta asignación ya se encuentra inactiva."
            );

            return "redirect:/branch-specialty";
        }

        // PREPARAR AUDITORÍA

        String descripcion =
                "Eliminó la especialidad "
                        + asignacion.getEspecialidad().getNombre()
                        + " de la sede "
                        + asignacion.getSucursal().getNombre();

        // DESACTIVACIÓN LÓGICA

        asignacion.setActivo(false);

        asignacionRepository.save(asignacion);

        // REGISTRAR AUDITORÍA

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                (String) session.getAttribute("usuario"),
                "ELIMINAR_ASIGNACION",
                null,
                descripcion
        );

        auditoriaRepository.save(auditoria);

        // MENSAJE DE CONFIRMACIÓN

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Asignación eliminada correctamente."
        );

        return "redirect:/branch-specialty";
    }

    // ==========================================
    // CARGAR CATÁLOGOS ACTIVOS
    // ==========================================

    private void cargarCatalogos(Model model) {

        model.addAttribute(
                "sucursales",
                sucursalRepository.findByActivoTrueOrderByNombreAsc()
        );

        model.addAttribute(
                "especialidades",
                especialidadRepository.findByActivoTrueOrderByNombreAsc()
        );
    }
}