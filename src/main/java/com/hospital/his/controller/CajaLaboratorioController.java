package com.hospital.his.controller;

import com.hospital.his.entity.OrdenLaboratorio;
import com.hospital.his.entity.Pago;

import com.hospital.his.repository.OrdenLaboratorioRepository;
import com.hospital.his.repository.PagoRepository;

import com.hospital.his.service.CajaLaboratorioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class CajaLaboratorioController {

    private final CajaLaboratorioService cajaLaboratorioService;
    private final OrdenLaboratorioRepository ordenRepository;
    private final PagoRepository pagoRepository;

    public CajaLaboratorioController(
            CajaLaboratorioService cajaLaboratorioService,
            OrdenLaboratorioRepository ordenRepository,
            PagoRepository pagoRepository) {

        this.cajaLaboratorioService = cajaLaboratorioService;
        this.ordenRepository = ordenRepository;
        this.pagoRepository = pagoRepository;
    }

    // ==========================================
    // VALIDAR SESIÓN DEL CAJERO
    // ==========================================

    private boolean esCajero(HttpSession session) {

        Object usuario = session.getAttribute("usuario");
        Object rol = session.getAttribute("rol");

        return usuario != null
                && rol instanceof String
                && "CAJERO".equalsIgnoreCase((String) rol);
    }

    // ==========================================
    // CU-10: BUSCAR ÓRDENES POR DPI
    // ==========================================

    @ResponseBody
    @Transactional(readOnly = true)
    @GetMapping("/api/caja/laboratorio/dpi/{dpi}")
    public Map<String, Object> buscarOrdenesPorDpi(
            @PathVariable String dpi,
            HttpSession session) {

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "No tiene permisos para consultar órdenes."
            );
        }

        if (dpi == null || !dpi.matches("[0-9]{13}")) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "El DPI debe contener exactamente 13 dígitos."
            );
        }

        List<OrdenLaboratorio> ordenes =
                ordenRepository.buscarPendientesPorDpi(dpi);

        List<Map<String, Object>> resultados = new ArrayList<>();

        for (OrdenLaboratorio orden : ordenes) {

            if (pagoRepository.existsByOrdenLaboratorioId(orden.getId())) {
                continue;
            }

            Map<String, Object> detalle = new LinkedHashMap<>();

            detalle.put("ordenId", orden.getId());

            detalle.put(
                    "paciente",
                    orden.getCita()
                            .getPaciente()
                            .getNombreCompleto()
            );

            detalle.put("dpi", dpi);

            detalle.put(
                    "cantidadExamenes",
                    orden.getExamenes() == null
                            ? 0
                            : orden.getExamenes().size()
            );

            detalle.put("fechaCreacion", orden.getFechaCreacion());
            detalle.put("estadoOrden", orden.getEstado());
            detalle.put("monto", orden.getMontoTotal());

            resultados.add(detalle);
        }

        if (resultados.isEmpty()) {

            return Map.of(
                    "estado", "SIN_RESULTADOS",
                    "mensaje",
                    "El paciente no tiene órdenes de laboratorio pendientes de pago."
            );
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("estado", "OK");
        respuesta.put("dpi", dpi);
        respuesta.put("cantidadOrdenes", resultados.size());
        respuesta.put("ordenes", resultados);

        return respuesta;
    }

    // ==========================================
    // CU-10: BUSCAR ORDEN POR NÚMERO
    // ==========================================

    @ResponseBody
    @Transactional(readOnly = true)
    @GetMapping("/api/caja/laboratorio/orden/{id}")
    public Map<String, Object> buscarOrden(
            @PathVariable Long id,
            HttpSession session) {

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "No tiene permisos para consultar órdenes."
            );
        }

        OrdenLaboratorio orden = ordenRepository.findById(id)
                .orElse(null);

        if (orden == null) {

            return Map.of(
                    "estado", "NO_ENCONTRADA",
                    "mensaje", "La orden de laboratorio no existe."
            );
        }

        boolean pagada = orden.getFechaPago() != null
                || pagoRepository.existsByOrdenLaboratorioId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("estado", "OK");
        respuesta.put("ordenId", orden.getId());

        respuesta.put(
                "paciente",
                orden.getCita()
                        .getPaciente()
                        .getNombreCompleto()
        );

        respuesta.put(
                "medico",
                orden.getCita()
                        .getMedico()
                        .getUsuario()
                        .getNombreCompleto()
        );

        respuesta.put(
                "dpi",
                orden.getCita()
                        .getPaciente()
                        .getDpi()
        );

        respuesta.put(
                "cantidadExamenes",
                orden.getExamenes() == null
                        ? 0
                        : orden.getExamenes().size()
        );

        respuesta.put("fechaCreacion", orden.getFechaCreacion());
        respuesta.put("estadoOrden", orden.getEstado());
        respuesta.put("monto", orden.getMontoTotal());
        respuesta.put("pagada", pagada);

        return respuesta;
    }

    // ==========================================
    // CU-10: REGISTRAR PAGO DE LABORATORIO
    // ==========================================

    @ResponseBody
    @PostMapping("/api/caja/laboratorio/pagar")
    public Map<String, Object> pagarLaboratorio(
            @RequestBody Map<String, Object> datos,
            HttpSession session) {

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "No tiene permisos para registrar pagos."
            );
        }

        try {

            // ==================================
            // 1. VALIDAR NÚMERO DE ORDEN
            // ==================================

            Object valorOrden = datos.get("ordenId");

            if (valorOrden == null) {

                throw new IllegalArgumentException(
                        "Debe indicar el número de orden."
                );
            }

            Long ordenId = Long.valueOf(
                    valorOrden.toString()
            );

            // ==================================
            // 2. VALIDAR MÉTODO DE PAGO
            // ==================================

            Object valorMetodo = datos.get("metodoPago");

            if (valorMetodo == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar un método de pago."
                );
            }

            String metodoPago = valorMetodo.toString()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            // ==================================
            // 3. OBTENER MONTO RECIBIDO
            // ==================================

            BigDecimal montoRecibido = null;

            Object valorRecibido = datos.get("montoRecibido");

            if (valorRecibido != null
                    && !valorRecibido.toString().isBlank()) {

                montoRecibido = new BigDecimal(
                        valorRecibido.toString()
                );
            }

            // ==================================
            // 4. VALIDAR DATOS DE TARJETA
            // ==================================

            String referencia = null;

            boolean esTarjeta =
                    metodoPago.equals("VISA")
                            || metodoPago.equals("MASTERCARD")
                            || metodoPago.equals("DEBITO");

            if (esTarjeta) {

                Object valorUltimos4 = datos.get("ultimos4");
                Object valorAutorizacion = datos.get("referencia");

                String ultimos4 = valorUltimos4 == null
                        ? ""
                        : valorUltimos4.toString().trim();

                String autorizacion = valorAutorizacion == null
                        ? ""
                        : valorAutorizacion.toString().trim();

                if (!ultimos4.matches("[0-9]{4}")) {

                    throw new IllegalArgumentException(
                            "Debe ingresar exactamente los últimos " +
                                    "4 dígitos de la tarjeta."
                    );
                }

                if (autorizacion.isBlank()) {

                    throw new IllegalArgumentException(
                            "Debe ingresar la referencia de una " +
                                    "transacción previamente autorizada."
                    );
                }

                if (autorizacion.contains("|")) {

                    throw new IllegalArgumentException(
                            "La referencia no puede contener el carácter |."
                    );
                }

                referencia = ultimos4 + "|" + autorizacion;
            }

            // ==================================
            // 5. REGISTRAR PAGO
            // ==================================

            Pago pago = cajaLaboratorioService.registrarPagoLaboratorio(
                    ordenId,
                    metodoPago,
                    montoRecibido,
                    referencia
            );

            // ==================================
            // 6. PREPARAR RESPUESTA
            // ==================================

            Map<String, Object> respuesta = new LinkedHashMap<>();

            respuesta.put("estado", "OK");

            respuesta.put(
                    "mensaje",
                    "Pago de laboratorio registrado correctamente."
            );

            respuesta.put("pagoId", pago.getId());
            respuesta.put("ordenId", ordenId);
            respuesta.put("monto", pago.getMonto());
            respuesta.put("metodoPago", pago.getMetodoPago());
            respuesta.put("cambio", pago.getCambio());

            if (esTarjeta) {

                String[] partes = pago.getReferencia().split("\\|", 2);

                respuesta.put("ultimos4", partes[0]);
                respuesta.put("referencia", partes[1]);

            } else {

                respuesta.put(
                        "referencia",
                        "PAGO-" + pago.getId()
                );
            }

            return respuesta;

        } catch (IllegalArgumentException | IllegalStateException e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", e.getMessage()
            );
        }
    }

    // ==========================================
    // CU-10: FA02 - PACIENTE NO PUEDE PAGAR
    // ==========================================

    @ResponseBody
    @PostMapping("/api/caja/laboratorio/no-puede-pagar/{id}")
    public Map<String, Object> noPuedePagarLaboratorio(
            @PathVariable Long id,
            HttpSession session) {

        // ======================================
        // 1. VALIDAR PERMISOS
        // ======================================

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }

        // ======================================
        // 2. VALIDAR NÚMERO DE ORDEN
        // ======================================

        if (id == null || id <= 0) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "El número de orden no es válido."
            );
        }

        // ======================================
        // 3. BUSCAR ORDEN
        // ======================================

        OrdenLaboratorio orden = ordenRepository.findById(id)
                .orElse(null);

        if (orden == null) {

            return Map.of(
                    "estado", "NO_ENCONTRADA",
                    "mensaje", "La orden de laboratorio no existe."
            );
        }

        // ======================================
        // 4. VERIFICAR ESTADO PENDIENTE
        // ======================================

        if (!"PENDIENTE".equalsIgnoreCase(orden.getEstado())) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Solo se puede indicar falta de pago " +
                            "en órdenes pendientes."
            );
        }

        // ======================================
        // 5. VERIFICAR QUE NO ESTÉ PAGADA
        // ======================================

        if (orden.getFechaPago() != null ||
                pagoRepository.existsByOrdenLaboratorioId(id)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La orden ya tiene un pago registrado."
            );
        }

        // ======================================
        // 6. NO REGISTRAR NINGÚN PAGO
        // ======================================

        // No se crea ningún objeto Pago.
        // No se modifica la orden.
        // No se cambia su estado.
        // No se registra fecha de pago.

        // ======================================
        // 7. MOSTRAR MENSAJE AL CAJERO
        // ======================================

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("estado", "OK");
        respuesta.put("ordenId", orden.getId());
        respuesta.put("estadoOrden", orden.getEstado());
        respuesta.put("pagada", false);

        respuesta.put(
                "mensaje",
                "El paciente no puede realizar el pago en este momento. " +
                        "La orden permanece PENDIENTE. " +
                        "Debe informar al médico solicitante."
        );

        return respuesta;
    }
}