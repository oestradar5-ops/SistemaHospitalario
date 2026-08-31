package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.Pago;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.PagoRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Controller
public class CajaController {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoRepository pagoRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public CajaController(
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            PagoRepository pagoRepository) {

        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoRepository = pagoRepository;
    }


    // =====================================================
    // VALIDAR CAJERO
    // =====================================================

    private boolean esCajero(
            HttpSession session) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        return usuario != null
                && "CAJERO".equalsIgnoreCase(rol);
    }


    // =====================================================
    // MOSTRAR MÓDULO DE CAJA
    // =====================================================

    @GetMapping("/caja")
    public String mostrarCaja(
            HttpSession session,
            Model model) {

        if (!esCajero(session)) {

            return "redirect:/caja/login";
        }


        model.addAttribute(
                "usuarioActual",
                session.getAttribute("usuario")
        );


        return "caja/index";
    }


    // =====================================================
    // BUSCAR CITA POR NÚMERO
    // =====================================================

    @ResponseBody
    @GetMapping("/api/caja/cita/{id}")
    public Map<String, Object> buscarPorCita(
            @PathVariable Long id,
            HttpSession session) {

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        Cita cita =
                citaRepository
                        .findById(id)
                        .orElse(null);


        if (cita == null ||
                !"PENDIENTE_DE_PAGO"
                        .equalsIgnoreCase(
                                cita.getEstado()
                        )) {

            return Map.of(
                    "estado", "NO_ENCONTRADA",
                    "mensaje",
                    "No se encontraron citas pendientes de pago para el criterio ingresado."
            );
        }


        return construirRespuestaCita(cita);
    }


    // =====================================================
    // BUSCAR CITA POR DPI
    // =====================================================

    @ResponseBody
    @GetMapping("/api/caja/dpi/{dpi}")
    public Map<String, Object> buscarPorDpi(
            @PathVariable String dpi,
            HttpSession session) {

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        Usuario paciente =
                usuarioRepository
                        .findByDpi(dpi)
                        .orElse(null);


        if (paciente == null) {

            return Map.of(
                    "estado", "NO_ENCONTRADA",
                    "mensaje",
                    "No se encontraron citas pendientes de pago para el criterio ingresado."
            );
        }


        List<Cita> citas =
                citaRepository
                        .findByPacienteIdOrderByFechaCreacionDesc(
                                paciente.getId()
                        );


        Cita citaPendiente =
                citas.stream()
                        .filter(cita ->
                                "PENDIENTE_DE_PAGO"
                                        .equalsIgnoreCase(
                                                cita.getEstado()
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        if (citaPendiente == null) {

            return Map.of(
                    "estado", "NO_ENCONTRADA",
                    "mensaje",
                    "No se encontraron citas pendientes de pago para el criterio ingresado."
            );
        }


        return construirRespuestaCita(
                citaPendiente
        );
    }


    // =====================================================
    // PROCESAR PAGO
    // =====================================================

    @ResponseBody
    @PostMapping("/api/caja/pagar")
    @Transactional
    public Map<String, Object> registrarPago(
            @RequestBody Map<String, Object> datos,
            HttpSession session) {

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        Long citaId;

        String metodoPago;


        try {

            citaId =
                    Long.valueOf(
                            datos.get("citaId")
                                    .toString()
                    );


            metodoPago =
                    datos.get("metodoPago")
                            .toString()
                            .trim()
                            .toUpperCase();

        }

        catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Los datos del pago están incompletos."
            );
        }


        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElse(null);


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (!"PENDIENTE_DE_PAGO"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita ya no se encuentra pendiente de pago."
            );
        }


        // =================================================
        // EVITAR PAGOS DUPLICADOS
        // =================================================

        if (pagoRepository.existsByCitaId(citaId)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Esta cita ya tiene un pago registrado."
            );
        }


        // =================================================
        // VALIDAR MÉTODO DE PAGO
        // =================================================

        if (!List.of(
                "EFECTIVO",
                "VISA",
                "MASTERCARD",
                "DEBITO"
        ).contains(metodoPago)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Método de pago no válido."
            );
        }


        // =================================================
        // MONTO ACTUAL DE LA CONSULTA
        // =================================================

        BigDecimal total =
                new BigDecimal("250.00");


        BigDecimal montoRecibido =
                null;


        BigDecimal cambio =
                null;


        String referenciaTarjeta =
                null;


        // =================================================
        // PAGO EN EFECTIVO
        // =================================================

        if ("EFECTIVO".equals(metodoPago)) {

            try {

                Object montoDato =
                        datos.get(
                                "montoRecibido"
                        );


                if (montoDato == null) {

                    return Map.of(
                            "estado", "ERROR",
                            "mensaje",
                            "Debe ingresar el monto recibido."
                    );
                }


                montoRecibido =
                        new BigDecimal(
                                montoDato.toString()
                        );

            }

            catch (Exception e) {

                return Map.of(
                        "estado", "ERROR",
                        "mensaje",
                        "Debe ingresar un monto recibido válido."
                );
            }


            // Evitar valores cero o negativos

            if (montoRecibido
                    .compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                return Map.of(
                        "estado", "ERROR",
                        "mensaje",
                        "El monto recibido debe ser mayor que cero."
                );
            }


            // Monto insuficiente

            if (montoRecibido
                    .compareTo(total) < 0) {

                return Map.of(
                        "estado", "ERROR",

                        "mensaje",
                        "El monto recibido (Q" +
                                montoRecibido +
                                ") es menor al monto a cobrar (Q" +
                                total +
                                ")."
                );
            }


            // Calcular cambio

            cambio =
                    montoRecibido
                            .subtract(total);
        }


        // =================================================
        // PAGO CON TARJETA
        // =================================================

        else {

            Object referencia =
                    datos.get(
                            "ultimos4"
                    );


            if (referencia == null ||
                    !referencia
                            .toString()
                            .matches("\\d{4}")) {

                return Map.of(
                        "estado", "ERROR",
                        "mensaje",
                        "Debe ingresar los últimos 4 dígitos de la tarjeta."
                );
            }


            referenciaTarjeta =
                    referencia.toString();


            // =================================================
            // FA04 - SIMULACIÓN DE TARJETA RECHAZADA
            // =================================================

            if ("0000".equals(
                    referenciaTarjeta
            )) {

                return Map.of(
                        "estado", "RECHAZADO",

                        "mensaje",
                        "La transacción con tarjeta fue rechazada por el banco. " +
                                "Solicite al paciente otro método de pago."
                );
            }
        }


        // =================================================
        // CREAR PAGO
        // =================================================

        Pago pago =
                new Pago();


        pago.setCita(
                cita
        );


        pago.setMonto(
                total
        );


        pago.setMetodoPago(
                metodoPago
        );


        pago.setEstado(
                "APROBADO"
        );


        pago.setFechaPago(
                LocalDateTime.now()
        );


        pago.setMontoRecibido(
                montoRecibido
        );


        pago.setCambio(
                cambio
        );


        // =================================================
        // GENERAR REFERENCIA
        // =================================================

        String referencia =
                "CAJA-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();


        if (referenciaTarjeta != null) {

            referencia =
                    referencia +
                            "-" +
                            referenciaTarjeta;
        }


        pago.setReferencia(
                referencia
        );


        // =================================================
        // GUARDAR PAGO
        // =================================================

        pagoRepository.save(
                pago
        );


        // =================================================
        // ACTUALIZAR CITA
        // =================================================

        cita.setEstado(
                "CONFIRMADA"
        );


        citaRepository.save(
                cita
        );


        // =================================================
        // RESPUESTA EFECTIVO
        // =================================================

        if ("EFECTIVO".equals(metodoPago)) {

            return Map.of(
                    "estado", "OK",

                    "mensaje",
                    "¡Pago registrado exitosamente! Paciente: " +
                            cita.getPaciente()
                                    .getNombreCompleto() +
                            ". La cita ha sido actualizada a estado Confirmada.",

                    "referencia",
                    referencia,

                    "monto",
                    total,

                    "montoRecibido",
                    montoRecibido,

                    "cambio",
                    cambio,

                    "metodoPago",
                    metodoPago
            );
        }


        // =================================================
        // RESPUESTA TARJETA
        // =================================================

        return Map.of(
                "estado", "OK",

                "mensaje",
                "¡Pago registrado exitosamente! Paciente: " +
                        cita.getPaciente()
                                .getNombreCompleto() +
                        ". La cita ha sido actualizada a estado Confirmada.",

                "referencia",
                referencia,

                "monto",
                total,

                "metodoPago",
                metodoPago
        );
    }


    // =====================================================
    // FA03 - PACIENTE NO PUEDE REALIZAR EL PAGO
    // =====================================================

    @ResponseBody
    @PostMapping(
            "/api/caja/no-puede-pagar/{citaId}"
    )
    @Transactional
    public Map<String, Object> pacienteNoPuedePagar(
            @PathVariable Long citaId,
            HttpSession session) {

        // =================================================
        // VALIDAR CAJERO
        // =================================================

        if (!esCajero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElse(null);


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        // =================================================
        // VALIDAR QUE SIGA PENDIENTE
        // =================================================

        if (!"PENDIENTE_DE_PAGO"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita ya no se encuentra pendiente de pago."
            );
        }


        // =================================================
        // VERIFICAR QUE NO TENGA PAGO
        // =================================================

        if (pagoRepository
                .existsByCitaId(
                        citaId
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita ya tiene un pago registrado."
            );
        }


        /*
         * IMPORTANTE:
         *
         * No se crea ningún registro en pagos.
         * No se cambia el estado de la cita.
         *
         * La cita permanece:
         *
         * PENDIENTE_DE_PAGO
         */


        return Map.of(
                "estado", "OK",

                "mensaje",
                "Operación cancelada. El paciente indicó que no puede realizar el pago.",

                "citaId",
                cita.getId(),

                "estadoCita",
                cita.getEstado()
        );
    }


    // =====================================================
    // CONSTRUIR RESPUESTA DE CITA
    // =====================================================

    private Map<String, Object> construirRespuestaCita(
            Cita cita) {

        return Map.ofEntries(

                Map.entry(
                        "estado",
                        "OK"
                ),

                Map.entry(
                        "citaId",
                        cita.getId()
                ),

                Map.entry(
                        "paciente",
                        cita.getPaciente()
                                .getNombreCompleto()
                ),

                Map.entry(
                        "dpi",
                        cita.getPaciente()
                                .getDpi()
                ),

                Map.entry(
                        "especialidad",
                        cita.getEspecialidad()
                                .getNombre()
                ),

                Map.entry(
                        "medico",
                        cita.getMedico()
                                .getUsuario()
                                .getNombreCompleto()
                ),

                Map.entry(
                        "fecha",
                        cita.getHorario()
                                .getFecha()
                                .toString()
                ),

                Map.entry(
                        "hora",
                        cita.getHorario()
                                .getHora()
                                .toString()
                ),

                Map.entry(
                        "estadoCita",
                        cita.getEstado()
                ),

                Map.entry(
                        "monto",
                        new BigDecimal(
                                "250.00"
                        )
                )
        );
    }
}