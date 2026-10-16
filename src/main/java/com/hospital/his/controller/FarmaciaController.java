package com.hospital.his.controller;

import com.hospital.his.dto.FarmaciaDespachoRequest;
import com.hospital.his.dto.FarmaciaRechazoRequest;

import com.hospital.his.entity.*;

import com.hospital.his.repository.*;

import com.hospital.his.service.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/farmacia")
public class FarmaciaController {

    // ==========================================
    // SERVICIOS
    // ==========================================

    private final FarmaciaSesionService farmaciaSesionService;

    private final FarmaciaRecetaService farmaciaRecetaService;

    private final FarmaciaDespachoService farmaciaDespachoService;

    private final FarmaciaRechazoService farmaciaRechazoService;

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final InventarioMedicamentoRepository inventarioRepository;

    private final DespachoMedicamentoRepository despachoRepository;

    private final DetalleDespachoRepository detalleDespachoRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaController(
            FarmaciaSesionService farmaciaSesionService,
            FarmaciaRecetaService farmaciaRecetaService,
            FarmaciaDespachoService farmaciaDespachoService,
            FarmaciaRechazoService farmaciaRechazoService,
            InventarioMedicamentoRepository inventarioRepository,
            DespachoMedicamentoRepository despachoRepository,
            DetalleDespachoRepository detalleDespachoRepository) {

        this.farmaciaSesionService = farmaciaSesionService;

        this.farmaciaRecetaService = farmaciaRecetaService;

        this.farmaciaDespachoService = farmaciaDespachoService;

        this.farmaciaRechazoService = farmaciaRechazoService;

        this.inventarioRepository = inventarioRepository;

        this.despachoRepository = despachoRepository;

        this.detalleDespachoRepository = detalleDespachoRepository;
    }

    // ==========================================
    // OBTENER SESIÓN EXISTENTE
    // ==========================================

    private HttpSession obtenerSesion(HttpServletRequest request) {

        return request.getSession(false);
    }

    // ==========================================
    // PANEL PRINCIPAL DE FARMACIA
    // ==========================================

    @GetMapping({"", "/"})
    public String panel(
            HttpServletRequest request,
            Model model) {

        try {

            HttpSession sesion = obtenerSesion(request);

            Usuario empleado =
                    farmaciaSesionService.obtenerUsuario(sesion);

            Sucursal sucursal =
                    farmaciaSesionService.obtenerSucursal(sesion);

            model.addAttribute(
                    "empleado",
                    empleado
            );

            model.addAttribute(
                    "sucursal",
                    sucursal
            );

            model.addAttribute(
                    "inventario",
                    inventarioRepository
                            .findBySucursalIdAndActivoTrue(
                                    sucursal.getId()
                            )
            );

            model.addAttribute(
                    "stockBajo",
                    inventarioRepository
                            .buscarStockBajo(
                                    sucursal.getId()
                            )
            );

            return "farmacia/panel-farmacia";

        } catch (SecurityException ex) {

            return "redirect:/farmacia/login";

        } catch (IllegalStateException ex) {

            model.addAttribute("error", ex.getMessage());

            return "farmacia/panel-farmacia";
        }
    }

    // ==========================================
    // PANTALLA DE NUEVO DESPACHO
    // ==========================================

    @GetMapping("/despacho/nuevo")
    public String nuevoDespacho(
            HttpServletRequest request,
            Model model) {

        try {

            HttpSession sesion = obtenerSesion(request);

            Usuario empleado =
                    farmaciaSesionService.obtenerUsuario(sesion);

            Sucursal sucursal =
                    farmaciaSesionService.obtenerSucursal(sesion);

            model.addAttribute("empleado", empleado);

            model.addAttribute("sucursal", sucursal);

            return "farmacia/nuevo-despacho";

        } catch (SecurityException ex) {

            return "redirect:/farmacia/login";

        } catch (IllegalStateException ex) {

            model.addAttribute("error", ex.getMessage());

            return "farmacia/nuevo-despacho";
        }
    }

    // ==========================================
    // BUSCAR RECETA POR ID
    // ==========================================

    @GetMapping("/api/recetas/{recetaId}")
    @ResponseBody
    public ResponseEntity<?> buscarReceta(
            @PathVariable Long recetaId,
            HttpServletRequest request) {

        try {

            HttpSession sesion = obtenerSesion(request);

            Sucursal sucursal =
                    farmaciaSesionService.obtenerSucursal(sesion);

            RecetaMedica receta =
                    farmaciaRecetaService.buscarPorId(recetaId);

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("recetaId", receta.getId());

            respuesta.put("estado", receta.getEstado());

            respuesta.put(
                    "fechaCreacion",
                    receta.getFechaCreacion()
            );

            respuesta.put(
                    "fechaVencimiento",
                    farmaciaRecetaService
                            .obtenerFechaVencimiento(receta)
            );

            respuesta.put(
                    "vigente",
                    farmaciaRecetaService.estaVigente(receta)
            );

            respuesta.put(
                    "sucursal",
                    sucursal.getNombre()
            );

            respuesta.put(
                    "medicamentos",
                    receta.getMedicamentos().stream()
                            .map(detalle -> {

                                Map<String, Object> medicamento =
                                        new HashMap<>();

                                medicamento.put(
                                        "recetaMedicamentoId",
                                        detalle.getId()
                                );

                                medicamento.put(
                                        "medicamentoId",
                                        detalle.getMedicamento().getId()
                                );

                                medicamento.put(
                                        "nombre",
                                        detalle.getMedicamento().getNombre()
                                );

                                medicamento.put(
                                        "cantidadRecetada",
                                        detalle.getCantidad()
                                );

                                medicamento.put(
                                        "dosis",
                                        detalle.getDosis()
                                );

                                medicamento.put(
                                        "frecuencia",
                                        detalle.getFrecuencia()
                                );

                                medicamento.put(
                                        "duracion",
                                        detalle.getDuracion()
                                );

                                Long cantidadDespachada =
                                        detalleDespachoRepository
                                                .obtenerCantidadDespachada(
                                                        detalle.getId()
                                                );

                                long entregada =
                                        cantidadDespachada == null
                                                ? 0
                                                : cantidadDespachada;

                                medicamento.put(
                                        "cantidadDespachada",
                                        entregada
                                );

                                medicamento.put(
                                        "cantidadPendiente",
                                        Math.max(
                                                0,
                                                detalle.getCantidad() - entregada
                                        )
                                );

                                inventarioRepository
                                        .findByMedicamentoIdAndSucursalIdAndActivoTrue(
                                                detalle.getMedicamento().getId(),
                                                sucursal.getId()
                                        )
                                        .ifPresent(inventario -> {

                                            medicamento.put(
                                                    "stockDisponible",
                                                    inventario.getCantidadDisponible()
                                            );

                                            medicamento.put(
                                                    "precioUnitario",
                                                    inventario.getPrecioUnitario()
                                            );
                                        });

                                return medicamento;
                            })
                            .toList()
            );

            return ResponseEntity.ok(respuesta);

        } catch (SecurityException ex) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ==========================================
    // BUSCAR RECETAS POR CONSULTA
    // ==========================================

    @GetMapping("/api/consultas/{consultaId}/recetas")
    @ResponseBody
    public ResponseEntity<?> buscarPorConsulta(
            @PathVariable Long consultaId,
            HttpServletRequest request) {

        try {

            farmaciaSesionService.obtenerUsuario(
                    obtenerSesion(request)
            );

            List<RecetaMedica> recetas =
                    farmaciaRecetaService.buscarPorConsulta(
                            consultaId
                    );

            List<Map<String, Object>> resultado =
                    recetas.stream()
                            .map(receta -> {

                                Map<String, Object> item =
                                        new HashMap<>();

                                item.put("recetaId", receta.getId());

                                item.put("estado", receta.getEstado());

                                item.put(
                                        "fechaCreacion",
                                        receta.getFechaCreacion()
                                );

                                item.put(
                                        "vigente",
                                        farmaciaRecetaService.estaVigente(receta)
                                );

                                return item;
                            })
                            .toList();

            return ResponseEntity.ok(resultado);

        } catch (SecurityException ex) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ==========================================
    // CONFIRMAR DESPACHO
    // ==========================================

    @PostMapping("/api/despachos")
    @ResponseBody
    public ResponseEntity<?> confirmarDespacho(
            @RequestBody FarmaciaDespachoRequest solicitud,
            HttpServletRequest request) {

        try {

            DespachoMedicamento despacho =
                    farmaciaDespachoService.confirmarDespacho(
                            solicitud,
                            obtenerSesion(request)
                    );

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put(
                    "mensaje",
                    "Despacho registrado correctamente."
            );

            respuesta.put(
                    "despachoId",
                    despacho.getId()
            );

            respuesta.put(
                    "montoTotal",
                    despacho.getMontoTotal()
            );

            respuesta.put(
                    "estado",
                    despacho.getEstado()
            );

            return ResponseEntity.ok(respuesta);

        } catch (SecurityException ex) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ==========================================
    // CONSULTAR DESPACHOS DE LA SUCURSAL
    // ==========================================

    @GetMapping("/api/despachos")
    @ResponseBody
    public ResponseEntity<?> listarDespachos(
            HttpServletRequest request) {

        try {

            Sucursal sucursal =
                    farmaciaSesionService.obtenerSucursal(
                            obtenerSesion(request)
                    );

            List<Map<String, Object>> resultado =
                    despachoRepository
                            .findBySucursalIdOrderByFechaDespachoDesc(
                                    sucursal.getId()
                            )
                            .stream()
                            .map(despacho -> {

                                Map<String, Object> item =
                                        new HashMap<>();

                                item.put(
                                        "despachoId",
                                        despacho.getId()
                                );

                                item.put(
                                        "recetaId",
                                        despacho.getReceta().getId()
                                );

                                item.put(
                                        "fecha",
                                        despacho.getFechaDespacho()
                                );

                                item.put(
                                        "montoTotal",
                                        despacho.getMontoTotal()
                                );

                                item.put(
                                        "estado",
                                        despacho.getEstado()
                                );

                                return item;
                            })
                            .toList();

            return ResponseEntity.ok(resultado);

        } catch (SecurityException ex) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ==========================================
    // CONSULTAR MEDICAMENTOS CON STOCK BAJO
    // ==========================================

    @GetMapping("/api/stock-bajo")
    @ResponseBody
    public ResponseEntity<?> consultarStockBajo(
            HttpServletRequest request) {

        try {

            Sucursal sucursal =
                    farmaciaSesionService.obtenerSucursal(
                            obtenerSesion(request)
                    );

            List<Map<String, Object>> resultado =
                    inventarioRepository
                            .buscarStockBajo(sucursal.getId())
                            .stream()
                            .map(inventario -> {

                                Map<String, Object> item =
                                        new HashMap<>();

                                item.put(
                                        "medicamento",
                                        inventario.getMedicamento().getNombre()
                                );

                                item.put(
                                        "cantidadDisponible",
                                        inventario.getCantidadDisponible()
                                );

                                item.put(
                                        "stockMinimo",
                                        inventario.getStockMinimo()
                                );

                                return item;
                            })
                            .toList();

            return ResponseEntity.ok(resultado);

        } catch (SecurityException ex) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ==========================================
    // CU-11: REGISTRAR RECHAZO DE MEDICAMENTOS
    // ==========================================

    @PostMapping("/api/rechazos")
    @ResponseBody
    public ResponseEntity<?> registrarRechazo(
            @RequestBody FarmaciaRechazoRequest solicitud,
            HttpServletRequest request) {

        try {
            if (solicitud == null || solicitud.getRecetaId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debe indicar una receta válida."));
            }

            RechazoMedicamento rechazo = farmaciaRechazoService.registrarRechazo(
                    solicitud.getRecetaId(),
                    solicitud.getMotivo(),
                    obtenerSesion(request)
            );

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Rechazo registrado correctamente.",
                    "rechazoId", rechazo.getId(),
                    "recetaId", rechazo.getReceta().getId(),
                    "estado", rechazo.getEstado()
            ));

        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
    // =====================================================
// CU-11: CONSULTAR MEDICAMENTOS DISPONIBLES
// PARA SOLICITAR SUSTITUCIONES
// =====================================================

    @GetMapping("/api/medicamentos-disponibles")
    @ResponseBody
    public ResponseEntity<?> consultarMedicamentosDisponibles(
            HttpServletRequest request) {

        try {

            HttpSession sesion = obtenerSesion(request);

            Sucursal sucursal =
                    farmaciaSesionService.obtenerSucursal(sesion);

            List<Map<String, Object>> medicamentos =
                    inventarioRepository
                            .findBySucursalIdAndActivoTrue(
                                    sucursal.getId()
                            )
                            .stream()
                            .filter(inventario ->
                                    inventario.getCantidadDisponible() != null
                                            && inventario.getCantidadDisponible() > 0
                                            && inventario.getMedicamento() != null
                                            && Boolean.TRUE.equals(
                                            inventario.getMedicamento().getActivo()
                                    )
                            )
                            .map(inventario -> {

                                Map<String, Object> medicamento =
                                        new HashMap<>();

                                medicamento.put(
                                        "medicamentoId",
                                        inventario.getMedicamento().getId()
                                );

                                medicamento.put(
                                        "nombre",
                                        inventario.getMedicamento().getNombre()
                                );

                                medicamento.put(
                                        "presentacion",
                                        inventario.getMedicamento().getPresentacion()
                                );

                                medicamento.put(
                                        "concentracion",
                                        inventario.getMedicamento().getConcentracion()
                                );

                                medicamento.put(
                                        "stockDisponible",
                                        inventario.getCantidadDisponible()
                                );

                                medicamento.put(
                                        "precioUnitario",
                                        inventario.getPrecioUnitario()
                                );

                                return medicamento;

                            })
                            .toList();

            return ResponseEntity.ok(medicamentos);

        } catch (SecurityException ex) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
