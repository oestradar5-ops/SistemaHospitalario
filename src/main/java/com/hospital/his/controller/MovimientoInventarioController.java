package com.hospital.his.controller;

import com.hospital.his.dto.MovimientoInventarioRequest;

import com.hospital.his.entity.MovimientoInventario;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.CatalogoMedicamentoRepository;
import com.hospital.his.repository.SucursalRepository;
import com.hospital.his.repository.UsuarioRepository;

import com.hospital.his.service.MovimientoInventarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/inventario/movimientos")
public class MovimientoInventarioController {

    // ==========================================
    // SERVICIOS Y REPOSITORIOS
    // ==========================================

    private final MovimientoInventarioService movimientoService;

    private final UsuarioRepository usuarioRepository;

    private final CatalogoMedicamentoRepository medicamentoRepository;

    private final SucursalRepository sucursalRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public MovimientoInventarioController(
            MovimientoInventarioService movimientoService,
            UsuarioRepository usuarioRepository,
            CatalogoMedicamentoRepository medicamentoRepository,
            SucursalRepository sucursalRepository) {

        this.movimientoService = movimientoService;
        this.usuarioRepository = usuarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.sucursalRepository = sucursalRepository;
    }

    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private boolean esAdministrador(HttpSession sesion) {

        if (sesion == null) {
            return false;
        }

        Object nombreUsuario =
                sesion.getAttribute("usuario");

        Object rol =
                sesion.getAttribute("rol");

        // ==========================================
// VALIDAR DATOS DE LA SESION
// ==========================================

        if (!(nombreUsuario instanceof String)
                || !(rol instanceof String)) {

            return false;
        }

// ==========================================
// OBTENER USUARIO Y ROL
// ==========================================

        String nombre = (String) nombreUsuario;
        String rolActual = (String) rol;

// ==========================================
// VALIDAR NOMBRE DE USUARIO
// ==========================================

        if (nombre.isBlank()) {
            return false;
        }

        if (!"ADMIN".equalsIgnoreCase(rolActual)
                && !"ADMINISTRADOR".equalsIgnoreCase(rolActual)) {

            return false;
        }

        Usuario usuario = usuarioRepository
                .findByNombreUsuario(nombre)
                .orElse(null);

        return usuario != null
                && Boolean.TRUE.equals(usuario.getActivo())
                && (
                "ADMIN".equalsIgnoreCase(usuario.getRol())
                        || "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol())
        );
    }

    // ==========================================
    // CU-15: LISTAR MOVIMIENTOS
    // ==========================================

    @GetMapping
    public String listar(
            HttpSession sesion,
            Model model) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(sesion)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // CONSULTAR MOVIMIENTOS
        // ======================================

        List<MovimientoInventario> movimientos =
                movimientoService.listarMovimientos();

        // ======================================
        // ENVIAR MOVIMIENTOS A LA VISTA
        // ======================================

        model.addAttribute(
                "movimientos",
                movimientos
        );

        // ======================================
        // TOTAL DE MOVIMIENTOS
        // ======================================

        model.addAttribute(
                "totalMovimientos",
                movimientos.size()
        );

        // ======================================
        // MOVIMIENTOS ACTIVOS
        // ======================================

        model.addAttribute(
                "movimientosActivos",
                movimientos.stream()
                        .filter(m ->
                                Boolean.TRUE.equals(m.getActivo()))
                        .count()
        );

        // ======================================
        // MOVIMIENTOS INACTIVOS
        // ======================================

        model.addAttribute(
                "movimientosInactivos",
                movimientos.stream()
                        .filter(m ->
                                !Boolean.TRUE.equals(m.getActivo()))
                        .count()
        );

        // ======================================
        // MOSTRAR HISTORIAL
        // ======================================

        return "admin/inventario/movimientos/index";
    }

    // ==========================================
    // CU-15: FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/create")
    public String mostrarFormulario(
            HttpSession sesion,
            Model model) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(sesion)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // PREPARAR FORMULARIO
        // ======================================

        model.addAttribute(
                "movimiento",
                new MovimientoInventarioRequest()
        );

        // ======================================
        // CONSULTAR MEDICAMENTOS ACTIVOS
        // ======================================

        model.addAttribute(
                "medicamentos",
                medicamentoRepository
                        .findByActivoTrueOrderByNombreAsc()
        );

        // ======================================
        // CONSULTAR SUCURSALES ACTIVAS
        // ======================================

        model.addAttribute(
                "sucursales",
                sucursalRepository
                        .findByActivoTrueOrderByNombreAsc()
        );

        // ======================================
        // MOSTRAR FORMULARIO
        // ======================================

        return "admin/inventario/movimientos/form";
    }

    // ==========================================
    // CU-15: REGISTRAR MOVIMIENTO
    // ==========================================

    @PostMapping("/create")
    public String registrar(
            @ModelAttribute("movimiento")
            MovimientoInventarioRequest solicitud,
            HttpSession sesion,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(sesion)) {
            return "redirect:/admin/login";
        }

        try {

            // ==================================
            // REGISTRAR MOVIMIENTO
            // ==================================

            movimientoService.registrarMovimiento(
                    solicitud,
                    sesion
            );

            // ==================================
            // MENSAJE DE CONFIRMACIÓN
            // ==================================

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Movimiento registrado correctamente."
            );

            return "redirect:/admin/inventario/movimientos";

        } catch (IllegalArgumentException
                 | IllegalStateException
                 | SecurityException e) {

            // ==================================
            // MOSTRAR ERROR
            // ==================================

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/inventario/movimientos/create";
        }
    }

    // ==========================================
    // CU-15: DESACTIVAR MOVIMIENTO
    // ==========================================

    @PostMapping("/desactivar/{id}")
    public String desactivar(
            @PathVariable Long id,
            HttpSession sesion,
            RedirectAttributes redirectAttributes) {

        return cambiarEstado(
                id,
                false,
                sesion,
                redirectAttributes
        );
    }

    // ==========================================
    // CU-15: ACTIVAR MOVIMIENTO
    // ==========================================

    @PostMapping("/activar/{id}")
    public String activar(
            @PathVariable Long id,
            HttpSession sesion,
            RedirectAttributes redirectAttributes) {

        return cambiarEstado(
                id,
                true,
                sesion,
                redirectAttributes
        );
    }

    // ==========================================
    // CU-15: CAMBIAR ESTADO DEL MOVIMIENTO
    // ==========================================

    private String cambiarEstado(
            Long id,
            boolean activo,
            HttpSession sesion,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(sesion)) {
            return "redirect:/admin/login";
        }

        try {

            // ==================================
            // ACTIVAR O DESACTIVAR
            // ==================================

            movimientoService.cambiarEstado(
                    id,
                    activo,
                    sesion
            );

            // ==================================
            // MENSAJE DE CONFIRMACIÓN
            // ==================================

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    activo
                            ? "Movimiento activado correctamente."
                            : "Movimiento desactivado correctamente."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException
                 | SecurityException e) {

            // ==================================
            // MOSTRAR ERROR
            // ==================================

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/inventario/movimientos";
    }
}