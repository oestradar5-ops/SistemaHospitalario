package com.hospital.his.controller;

import com.hospital.his.dto.MovimientoInventarioRequest;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.CatalogoMedicamento;
import com.hospital.his.entity.InventarioMedicamento;
import com.hospital.his.entity.Sucursal;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.CatalogoMedicamentoRepository;
import com.hospital.his.repository.InventarioMedicamentoRepository;
import com.hospital.his.repository.SucursalRepository;

import com.hospital.his.service.MovimientoInventarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin/catalogos/inventario-medicamentos")
public class InventarioMedicamentoAdminController {

    // ==========================================
    // REPOSITORIOS Y SERVICIOS
    // ==========================================

    private final InventarioMedicamentoRepository inventarioRepository;

    private final CatalogoMedicamentoRepository medicamentoRepository;

    private final SucursalRepository sucursalRepository;

    private final AuditoriaUsuarioRepository auditoriaRepository;

    private final MovimientoInventarioService movimientoInventarioService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public InventarioMedicamentoAdminController(
            InventarioMedicamentoRepository inventarioRepository,
            CatalogoMedicamentoRepository medicamentoRepository,
            SucursalRepository sucursalRepository,
            AuditoriaUsuarioRepository auditoriaRepository,
            MovimientoInventarioService movimientoInventarioService) {

        this.inventarioRepository = inventarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.sucursalRepository = sucursalRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.movimientoInventarioService = movimientoInventarioService;
    }


    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private boolean esAdministrador(HttpSession session) {

        if (session == null) {
            return false;
        }

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
    // VALIDAR PRECIO UNITARIO
    // ==========================================

    private BigDecimal validarPrecio(String precioUnitario) {

        if (precioUnitario == null || precioUnitario.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar un precio unitario."
            );
        }

        try {

            BigDecimal precio =
                    new BigDecimal(precioUnitario.trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0
                    || precio.scale() > 2
                    || precio.precision() - precio.scale() > 8) {

                throw new IllegalArgumentException(
                        "El precio debe ser mayor que cero y tener máximo dos decimales."
                );
            }

            return precio;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "El precio unitario no tiene un formato válido."
            );
        }
    }


    // ==========================================
    // CU-14: LISTAR INVENTARIO
    // ==========================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listarInventario(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        List<InventarioMedicamento> inventarios =
                inventarioRepository.findAllConDetalles();

        List<InventarioMedicamento> stockBajo =
                inventarioRepository.buscarStockBajoGeneral();

        long totalRegistros = inventarios.size();

        long registrosActivos = inventarios.stream()
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .count();

        long registrosInactivos = inventarios.stream()
                .filter(i -> !Boolean.TRUE.equals(i.getActivo()))
                .count();

        long totalStockBajo = stockBajo.size();

        model.addAttribute("inventarios", inventarios);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("totalRegistros", totalRegistros);
        model.addAttribute("registrosActivos", registrosActivos);
        model.addAttribute("registrosInactivos", registrosInactivos);
        model.addAttribute("totalStockBajo", totalStockBajo);

        return "admin/catalogos/inventario-medicamentos/index";
    }


    // ==========================================
    // CU-14: FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/create")
    @Transactional(readOnly = true)
    public String mostrarRegistro(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        List<CatalogoMedicamento> medicamentos =
                medicamentoRepository.findByActivoTrueOrderByNombreAsc();

        List<Sucursal> sucursales =
                sucursalRepository.findByActivoTrueOrderByNombreAsc();

        InventarioMedicamento inventario =
                new InventarioMedicamento();

        inventario.setCantidadDisponible(0);
        inventario.setStockMinimo(5);
        inventario.setPrecioUnitario(BigDecimal.ZERO);
        inventario.setActivo(true);

        model.addAttribute("inventario", inventario);
        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("sucursales", sucursales);
        model.addAttribute("modoEdicion", false);

        return "admin/catalogos/inventario-medicamentos/form";
    }


    // ==========================================
    // CU-14: REGISTRAR INVENTARIO
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrarInventario(
            @RequestParam(required = false) Long medicamentoId,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Integer cantidadDisponible,
            @RequestParam(required = false) Integer stockMinimo,
            @RequestParam(required = false) String precioUnitario,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaFormulario =
                "redirect:/admin/catalogos/inventario-medicamentos/create";

        if (medicamentoId == null || sucursalId == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debe seleccionar un medicamento y una sucursal."
            );

            return rutaFormulario;
        }

        if (cantidadDisponible == null || cantidadDisponible < 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cantidad disponible no puede ser negativa."
            );

            return rutaFormulario;
        }

        if (stockMinimo == null || stockMinimo < 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El stock mínimo no puede ser negativo."
            );

            return rutaFormulario;
        }

        BigDecimal precio;

        try {
            precio = validarPrecio(precioUnitario);
        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return rutaFormulario;
        }

        CatalogoMedicamento medicamento =
                medicamentoRepository.findById(medicamentoId)
                        .orElse(null);

        if (medicamento == null
                || !Boolean.TRUE.equals(medicamento.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento no existe o está inactivo."
            );

            return rutaFormulario;
        }

        Sucursal sucursal =
                sucursalRepository.findById(sucursalId)
                        .orElse(null);

        if (sucursal == null
                || !Boolean.TRUE.equals(sucursal.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal no existe o está inactiva."
            );

            return rutaFormulario;
        }

        if (inventarioRepository.existsByMedicamentoIdAndSucursalId(
                medicamentoId,
                sucursalId)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este medicamento ya tiene inventario en la sucursal seleccionada."
            );

            return rutaFormulario;
        }

        InventarioMedicamento inventario =
                new InventarioMedicamento();

        inventario.setMedicamento(medicamento);
        inventario.setSucursal(sucursal);
        inventario.setCantidadDisponible(cantidadDisponible);
        inventario.setStockMinimo(stockMinimo);
        inventario.setPrecioUnitario(precio);
        inventario.setActivo(true);

        inventarioRepository.save(inventario);

        registrarAuditoria(
                session,
                "CREAR_INVENTARIO_MEDICAMENTO",
                "Registró inventario del medicamento: "
                        + medicamento.getNombre()
                        + " | Sucursal: " + sucursal.getNombre()
                        + " | Cantidad inicial: " + cantidadDisponible
                        + " | Stock mínimo: " + stockMinimo
                        + " | Precio: Q " + precio
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Inventario registrado correctamente."
        );

        return "redirect:/admin/catalogos/inventario-medicamentos";
    }


    // ==========================================
    // CU-14: FORMULARIO DE EDICIÓN
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

        InventarioMedicamento inventario =
                inventarioRepository.findById(id)
                        .orElse(null);

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El inventario solicitado no existe."
            );

            return "redirect:/admin/catalogos/inventario-medicamentos";
        }

        List<CatalogoMedicamento> medicamentos =
                medicamentoRepository.findByActivoTrueOrderByNombreAsc();

        List<Sucursal> sucursales =
                sucursalRepository.findByActivoTrueOrderByNombreAsc();

        model.addAttribute("inventario", inventario);
        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("sucursales", sucursales);
        model.addAttribute("modoEdicion", true);

        return "admin/catalogos/inventario-medicamentos/form";
    }


    // ==========================================
    // CU-14 + CU-15: ACTUALIZAR INVENTARIO
    // ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizarInventario(
            @PathVariable Long id,
            @RequestParam(required = false) Integer cantidadDisponible,
            @RequestParam(required = false) Integer cantidadOriginal,
            @RequestParam(required = false) Integer stockMinimo,
            @RequestParam(required = false) String precioUnitario,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaFormulario =
                "redirect:/admin/catalogos/inventario-medicamentos/edit/" + id;

        String rutaListado =
                "redirect:/admin/catalogos/inventario-medicamentos";

        // ======================================
        // VALIDAR CANTIDADES
        // ======================================

        if (cantidadDisponible == null || cantidadDisponible < 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cantidad disponible debe ser mayor o igual a cero."
            );

            return rutaFormulario;
        }

        if (stockMinimo == null || stockMinimo < 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El stock mínimo debe ser mayor o igual a cero."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR PRECIO
        // ======================================

        BigDecimal precio;

        try {
            precio = validarPrecio(precioUnitario);
        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return rutaFormulario;
        }

        // ======================================
        // BLOQUEAR INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaActualizar(id)
                        .orElse(null);

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario no existe."
            );

            return rutaListado;
        }

        if (!Boolean.TRUE.equals(inventario.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No puedes modificar un inventario inactivo."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR EXISTENCIAS ORIGINALES
        // ======================================

        if (cantidadOriginal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se pudo verificar la cantidad original. Vuelve a abrir el formulario."
            );

            return rutaFormulario;
        }

        if (!Objects.equals(
                cantidadOriginal,
                inventario.getCantidadDisponible())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las existencias cambiaron mientras editabas el inventario. "
                            + "Vuelve a abrir el formulario."
            );

            return rutaFormulario;
        }

        // ======================================
        // GUARDAR VALORES ANTERIORES
        // ======================================

        int cantidadAnterior =
                inventario.getCantidadDisponible();

        int stockMinimoAnterior =
                inventario.getStockMinimo();

        BigDecimal precioAnterior =
                inventario.getPrecioUnitario();

        // ======================================
        // CALCULAR DIFERENCIA DE EXISTENCIAS
        // ======================================

        long diferencia =
                (long) cantidadDisponible - cantidadAnterior;

        // ======================================
        // CU-15: REGISTRAR AJUSTE AUTOMÁTICO
        // ======================================

        if (diferencia != 0) {

            int tipoMovimiento;

            int cantidadMovimiento;

            if (diferencia > 0) {

                // Tipo 4: Ajuste positivo
                tipoMovimiento = 4;

            } else {

                // Tipo 5: Ajuste negativo
                tipoMovimiento = 5;
            }

            cantidadMovimiento = Math.toIntExact(
                    Math.abs(diferencia)
            );

            MovimientoInventarioRequest solicitud =
                    new MovimientoInventarioRequest();

            solicitud.setMedicamentoId(
                    inventario.getMedicamento().getId()
            );

            solicitud.setSucursalId(
                    inventario.getSucursal().getId()
            );

            solicitud.setTipoMovimiento(tipoMovimiento);

            solicitud.setCantidad(cantidadMovimiento);

            solicitud.setCostoUnitario(precio);

            solicitud.setNumeroReferencia(
                    "AJUSTE-ADMIN-" + id
            );

            solicitud.setMotivoNotas(
                    "Ajuste administrativo de inventario. "
                            + "Existencias anteriores: " + cantidadAnterior
                            + ". Existencias nuevas: " + cantidadDisponible
                            + ". Responsable: "
                            + session.getAttribute("usuario")
            );

            // IMPORTANTE:
            // Este servicio actualiza las existencias
            // y registra el movimiento en la bitácora.
            //
            // No debemos modificar directamente
            // cantidadDisponible en este controlador.

            movimientoInventarioService.registrarMovimiento(
                    solicitud,
                    session
            );
        }

        // ======================================
        // ACTUALIZAR STOCK MÍNIMO Y PRECIO
        // ======================================

        inventario.setStockMinimo(stockMinimo);

        inventario.setPrecioUnitario(precio);

        inventarioRepository.save(inventario);

        // ======================================
        // REGISTRAR AUDITORÍA ADMINISTRATIVA
        // ======================================

        registrarAuditoria(
                session,
                "ACTUALIZAR_INVENTARIO_MEDICAMENTO",
                "Actualizó inventario ID: " + inventario.getId()
                        + " | Medicamento ID: "
                        + inventario.getMedicamento().getId()
                        + " | Sucursal ID: "
                        + inventario.getSucursal().getId()
                        + " | Cantidad: "
                        + cantidadAnterior + " -> " + cantidadDisponible
                        + " | Stock mínimo: "
                        + stockMinimoAnterior + " -> " + stockMinimo
                        + " | Precio: Q "
                        + precioAnterior + " -> Q " + precio
        );

        // ======================================
        // CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                diferencia == 0
                        ? "Inventario actualizado correctamente."
                        : "Inventario actualizado y movimiento registrado en la bitácora."
        );

        return rutaListado;
    }


    // ==========================================
    // CU-14: DESACTIVAR INVENTARIO
    // ==========================================

    @PostMapping("/desactivar/{id}")
    @Transactional
    public String desactivarInventario(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/inventario-medicamentos";

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaActualizar(id)
                        .orElse(null);

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario no existe."
            );

            return rutaListado;
        }

        if (!Boolean.TRUE.equals(inventario.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este inventario ya está desactivado."
            );

            return rutaListado;
        }

        Long inventarioId = inventario.getId();

        Long medicamentoId =
                inventario.getMedicamento().getId();

        Long sucursalId =
                inventario.getSucursal().getId();

        Integer cantidadDisponible =
                inventario.getCantidadDisponible();

        inventario.setActivo(false);

        inventarioRepository.save(inventario);

        registrarAuditoria(
                session,
                "DESACTIVAR_INVENTARIO_MEDICAMENTO",
                "Desactivó inventario ID: " + inventarioId
                        + " | Medicamento ID: " + medicamentoId
                        + " | Sucursal ID: " + sucursalId
                        + " | Existencias conservadas: "
                        + cantidadDisponible
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Inventario desactivado correctamente."
        );

        return rutaListado;
    }


    // ==========================================
    // CU-14: ACTIVAR INVENTARIO
    // ==========================================

    @PostMapping("/activar/{id}")
    @Transactional
    public String activarInventario(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/inventario-medicamentos";

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaActualizar(id)
                        .orElse(null);

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario no existe."
            );

            return rutaListado;
        }

        if (Boolean.TRUE.equals(inventario.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este inventario ya está activo."
            );

            return rutaListado;
        }

        CatalogoMedicamento medicamento =
                inventario.getMedicamento();

        Sucursal sucursal =
                inventario.getSucursal();

        if (medicamento == null
                || !Boolean.TRUE.equals(medicamento.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede activar porque el medicamento está inactivo."
            );

            return rutaListado;
        }

        if (sucursal == null
                || !Boolean.TRUE.equals(sucursal.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede activar porque la sucursal está inactiva."
            );

            return rutaListado;
        }

        inventario.setActivo(true);

        inventarioRepository.save(inventario);

        registrarAuditoria(
                session,
                "ACTIVAR_INVENTARIO_MEDICAMENTO",
                "Reactivó inventario ID: " + inventario.getId()
                        + " | Medicamento: " + medicamento.getNombre()
                        + " | Sucursal: " + sucursal.getNombre()
                        + " | Existencias conservadas: "
                        + inventario.getCantidadDisponible()
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Inventario reactivado correctamente."
        );

        return rutaListado;
    }
}