package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.CatalogoMedicamento;
import com.hospital.his.entity.InventarioMedicamento;
import com.hospital.his.entity.Sucursal;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.CatalogoMedicamentoRepository;
import com.hospital.his.repository.InventarioMedicamentoRepository;
import com.hospital.his.repository.SucursalRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/catalogos/inventario-medicamentos")
public class InventarioMedicamentoAdminController {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final InventarioMedicamentoRepository inventarioRepository;

    private final CatalogoMedicamentoRepository medicamentoRepository;

    private final SucursalRepository sucursalRepository;

    private final AuditoriaUsuarioRepository auditoriaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public InventarioMedicamentoAdminController(
            InventarioMedicamentoRepository inventarioRepository,
            CatalogoMedicamentoRepository medicamentoRepository,
            SucursalRepository sucursalRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.inventarioRepository = inventarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.sucursalRepository = sucursalRepository;
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
// CU-14: LISTAR INVENTARIO DE MEDICAMENTOS
// ==========================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listarInventario(
            HttpSession session,
            Model model) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // CONSULTAR INVENTARIO COMPLETO
        // ======================================

        List<InventarioMedicamento> inventarios =
                inventarioRepository.findAllConDetalles();

        // ======================================
        // CONSULTAR MEDICAMENTOS CON STOCK BAJO
        // ======================================

        List<InventarioMedicamento> stockBajo =
                inventarioRepository.buscarStockBajoGeneral();

        // ======================================
        // CALCULAR ESTADÍSTICAS
        // ======================================

        long totalRegistros = inventarios.size();

        long registrosActivos = inventarios.stream()
                .filter(inventario ->
                        Boolean.TRUE.equals(inventario.getActivo()))
                .count();

        long registrosInactivos = inventarios.stream()
                .filter(inventario ->
                        !Boolean.TRUE.equals(inventario.getActivo()))
                .count();

        long totalStockBajo = stockBajo.size();

        // ======================================
        // ENVIAR INFORMACIÓN A THYMELEAF
        // ======================================

        model.addAttribute(
                "inventarios",
                inventarios
        );

        model.addAttribute(
                "stockBajo",
                stockBajo
        );

        model.addAttribute(
                "totalRegistros",
                totalRegistros
        );

        model.addAttribute(
                "registrosActivos",
                registrosActivos
        );

        model.addAttribute(
                "registrosInactivos",
                registrosInactivos
        );

        model.addAttribute(
                "totalStockBajo",
                totalStockBajo
        );

        // ======================================
        // MOSTRAR PANTALLA ADMINISTRATIVA
        // ======================================

        return "admin/catalogos/inventario-medicamentos/index";
    }
    // ==========================================
// CU-14: FORMULARIO DE REGISTRO DE INVENTARIO
// ==========================================

    @GetMapping("/create")
    @Transactional(readOnly = true)
    public String mostrarRegistro(
            HttpSession session,
            Model model) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // CONSULTAR MEDICAMENTOS ACTIVOS
        // ======================================

        List<CatalogoMedicamento> medicamentos =
                medicamentoRepository
                        .findByActivoTrueOrderByNombreAsc();

        // ======================================
        // CONSULTAR SUCURSALES ACTIVAS
        // ======================================

        List<Sucursal> sucursales =
                sucursalRepository
                        .findByActivoTrueOrderByNombreAsc();

        // ======================================
        // PREPARAR NUEVO INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                new InventarioMedicamento();

        inventario.setCantidadDisponible(0);
        inventario.setStockMinimo(5);
        inventario.setPrecioUnitario(BigDecimal.ZERO);
        inventario.setActivo(true);

        // ======================================
        // ENVIAR DATOS AL FORMULARIO
        // ======================================

        model.addAttribute(
                "inventario",
                inventario
        );

        model.addAttribute(
                "medicamentos",
                medicamentos
        );

        model.addAttribute(
                "sucursales",
                sucursales
        );

        model.addAttribute(
                "modoEdicion",
                false
        );

        // ======================================
        // MOSTRAR FORMULARIO
        // ======================================

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

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaFormulario =
                "redirect:/admin/catalogos/inventario-medicamentos/create";

        // ======================================
        // VALIDAR CAMPOS OBLIGATORIOS
        // ======================================

        if (medicamentoId == null || sucursalId == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debes seleccionar un medicamento y una sucursal."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR CANTIDADES
        // ======================================

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

        // ======================================
        // VALIDAR PRECIO
        // ======================================

        BigDecimal precio;

        try {

            if (precioUnitario == null || precioUnitario.isBlank()) {
                throw new NumberFormatException();
            }

            precio = new BigDecimal(precioUnitario.trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0
                    || precio.scale() > 2
                    || precio.precision() - precio.scale() > 8) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El precio debe ser mayor que cero, "
                            + "con máximo dos decimales."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR MEDICAMENTO
        // ======================================

        CatalogoMedicamento medicamento =
                medicamentoRepository.findById(medicamentoId)
                        .orElse(null);

        if (medicamento == null
                || !Boolean.TRUE.equals(medicamento.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El medicamento seleccionado no existe o está inactivo."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR SUCURSAL
        // ======================================

        Sucursal sucursal =
                sucursalRepository.findById(sucursalId)
                        .orElse(null);

        if (sucursal == null
                || !Boolean.TRUE.equals(sucursal.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La sucursal seleccionada no existe o está inactiva."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR INVENTARIO DUPLICADO
        // ======================================

        if (inventarioRepository.existsByMedicamentoIdAndSucursalId(
                medicamentoId,
                sucursalId)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este medicamento ya tiene un registro de inventario "
                            + "en la sucursal seleccionada. "
                            + "Debes editar el registro existente."
            );

            return rutaFormulario;
        }

        // ======================================
        // CREAR REGISTRO DE INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                new InventarioMedicamento();

        inventario.setMedicamento(medicamento);
        inventario.setSucursal(sucursal);
        inventario.setCantidadDisponible(cantidadDisponible);
        inventario.setStockMinimo(stockMinimo);
        inventario.setPrecioUnitario(precio);
        inventario.setActivo(true);

        // ======================================
        // GUARDAR INVENTARIO
        // ======================================

        inventarioRepository.save(inventario);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

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

        // ======================================
        // MENSAJE DE CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Inventario registrado correctamente."
        );

        return "redirect:/admin/catalogos/inventario-medicamentos";
    }
    // ==========================================
// CU-14: FORMULARIO DE EDICIÓN DE INVENTARIO
// ==========================================

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String mostrarEdicion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // BUSCAR REGISTRO DE INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                inventarioRepository.findById(id)
                        .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario solicitado no existe."
            );

            return "redirect:/admin/catalogos/inventario-medicamentos";
        }

        // ======================================
        // CONSULTAR MEDICAMENTOS ACTIVOS
        // ======================================

        List<CatalogoMedicamento> medicamentos =
                medicamentoRepository
                        .findByActivoTrueOrderByNombreAsc();

        // ======================================
        // CONSULTAR SUCURSALES ACTIVAS
        // ======================================

        List<Sucursal> sucursales =
                sucursalRepository
                        .findByActivoTrueOrderByNombreAsc();

        // ======================================
        // PREPARAR FORMULARIO
        // ======================================

        model.addAttribute(
                "inventario",
                inventario
        );

        model.addAttribute(
                "medicamentos",
                medicamentos
        );

        model.addAttribute(
                "sucursales",
                sucursales
        );

        model.addAttribute(
                "modoEdicion",
                true
        );

        // ======================================
        // MOSTRAR FORMULARIO
        // ======================================

        return "admin/catalogos/inventario-medicamentos/form";
    }
    // ==========================================
// CU-14: ACTUALIZAR INVENTARIO
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
        // VALIDAR PRECIO UNITARIO
        // ======================================

        BigDecimal precio;

        try {

            if (precioUnitario == null || precioUnitario.isBlank()) {
                throw new NumberFormatException();
            }

            precio = new BigDecimal(precioUnitario.trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0
                    || precio.scale() > 2
                    || precio.precision() - precio.scale() > 8) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El precio debe ser mayor que cero, "
                            + "con máximo dos decimales."
            );

            return rutaFormulario;
        }

        // ======================================
        // BLOQUEAR REGISTRO DE INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaActualizar(id)
                        .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario no existe."
            );

            return "redirect:/admin/catalogos/inventario-medicamentos";
        }

        // ======================================
        // VALIDAR ESTADO DEL INVENTARIO
        // ======================================

        if (!Boolean.TRUE.equals(inventario.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No puedes modificar un inventario inactivo."
            );

            return "redirect:/admin/catalogos/inventario-medicamentos";
        }

        // ======================================
        // EVITAR SOBRESCRIBIR EXISTENCIAS DESACTUALIZADAS
        // ======================================

        if (cantidadOriginal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se pudo verificar la cantidad original del inventario. "
                            + "Vuelve a abrir el formulario."
            );

            return rutaFormulario;
        }

        if (!java.util.Objects.equals(
                cantidadOriginal,
                inventario.getCantidadDisponible())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las existencias cambiaron mientras editabas el inventario. "
                            + "Revisa la cantidad actual y vuelve a guardar."
            );

            return rutaFormulario;
        }

        // ======================================
        // GUARDAR VALORES ANTERIORES
        // ======================================

        Integer cantidadAnterior =
                inventario.getCantidadDisponible();

        Integer stockMinimoAnterior =
                inventario.getStockMinimo();

        BigDecimal precioAnterior =
                inventario.getPrecioUnitario();

        // ======================================
        // ACTUALIZAR INVENTARIO
        // ======================================

        inventario.setCantidadDisponible(cantidadDisponible);

        inventario.setStockMinimo(stockMinimo);

        inventario.setPrecioUnitario(precio);

        inventarioRepository.save(inventario);

        // ======================================
        // REGISTRAR AUDITORÍA
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
                "Inventario actualizado correctamente."
        );

        return "redirect:/admin/catalogos/inventario-medicamentos";
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

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/inventario-medicamentos";

        // ======================================
        // BLOQUEAR REGISTRO DE INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaActualizar(id)
                        .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario no existe."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR ESTADO ACTUAL
        // ======================================

        if (!Boolean.TRUE.equals(inventario.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este inventario ya se encuentra desactivado."
            );

            return rutaListado;
        }

        // ======================================
        // CONSERVAR INFORMACIÓN
        // ======================================

        Long inventarioId = inventario.getId();

        Long medicamentoId =
                inventario.getMedicamento().getId();

        Long sucursalId =
                inventario.getSucursal().getId();

        Integer cantidadDisponible =
                inventario.getCantidadDisponible();

        // ======================================
        // DESACTIVACIÓN LÓGICA
        // ======================================

        inventario.setActivo(false);

        inventarioRepository.save(inventario);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        registrarAuditoria(
                session,
                "DESACTIVAR_INVENTARIO_MEDICAMENTO",
                "Desactivó el inventario ID: " + inventarioId
                        + " | Medicamento ID: " + medicamentoId
                        + " | Sucursal ID: " + sucursalId
                        + " | Existencias conservadas: "
                        + cantidadDisponible
        );

        // ======================================
        // MENSAJE DE CONFIRMACIÓN
        // ======================================

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

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/inventario-medicamentos";

        // ======================================
        // BLOQUEAR REGISTRO DE INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaActualizar(id)
                        .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (inventario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El registro de inventario no existe."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR ESTADO ACTUAL
        // ======================================

        if (Boolean.TRUE.equals(inventario.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este inventario ya se encuentra activo."
            );

            return rutaListado;
        }

        // ======================================
        // OBTENER MEDICAMENTO Y SUCURSAL
        // ======================================

        CatalogoMedicamento medicamento =
                inventario.getMedicamento();

        Sucursal sucursal =
                inventario.getSucursal();

        // ======================================
        // VALIDAR MEDICAMENTO ACTIVO
        // ======================================

        if (medicamento == null
                || !Boolean.TRUE.equals(medicamento.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede activar el inventario porque "
                            + "el medicamento está desactivado."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR SUCURSAL ACTIVA
        // ======================================

        if (sucursal == null
                || !Boolean.TRUE.equals(sucursal.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede activar el inventario porque "
                            + "la sucursal está desactivada."
            );

            return rutaListado;
        }

        // ======================================
        // REACTIVAR INVENTARIO
        // ======================================

        inventario.setActivo(true);

        inventarioRepository.save(inventario);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        registrarAuditoria(
                session,
                "ACTIVAR_INVENTARIO_MEDICAMENTO",
                "Reactivó el inventario ID: " + inventario.getId()
                        + " | Medicamento: " + medicamento.getNombre()
                        + " | Sucursal: " + sucursal.getNombre()
                        + " | Existencias conservadas: "
                        + inventario.getCantidadDisponible()
        );

        // ======================================
        // MENSAJE DE CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Inventario reactivado correctamente."
        );

        return rutaListado;
    }

}
