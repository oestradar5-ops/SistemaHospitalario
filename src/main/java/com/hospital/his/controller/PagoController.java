package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.Pago;
import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.PagoRepository;

import jakarta.servlet.http.HttpSession;

import com.hospital.his.service.ComprobanteService;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Controller
public class PagoController {

    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;
    private final ComprobanteService comprobanteService;

    public PagoController(
            CitaRepository citaRepository,
            PagoRepository pagoRepository,
            ComprobanteService comprobanteService) {

        this.citaRepository = citaRepository;
        this.pagoRepository = pagoRepository;
        this.comprobanteService = comprobanteService;
    }


    // =====================================================
    // MOSTRAR PANTALLA DE PAGO
    // =====================================================

    @GetMapping("/paciente/pago")
    public String mostrarPago(
            @RequestParam Long citaId,
            HttpSession session,
            Model model) {

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");


        if (nombreUsuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return "redirect:/login";
        }


        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElse(null);


        if (cita == null) {

            return "redirect:/paciente/dashboard";
        }


        // Evitar que otro paciente intente pagar la cita
        if (cita.getPaciente() == null ||
                !nombreUsuario.equalsIgnoreCase(
                        cita.getPaciente().getNombreUsuario()
                )) {

            return "redirect:/paciente/dashboard";
        }


        model.addAttribute(
                "cita",
                cita
        );


        // Por ahora utilizaremos un monto fijo de prueba.
        // Más adelante puede venir de la especialidad.
        model.addAttribute(
                "monto",
                new BigDecimal("250.00")
        );


        return "paciente/pago";
    }


    // =====================================================
    // PROCESAR PAGO
    // =====================================================

    @ResponseBody
    @PostMapping("/api/pagos/procesar")
    @Transactional
    public Map<String, Object> procesarPago(
            @RequestBody Map<String, Object> datos,
            HttpSession session) {

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");


        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        if (nombreUsuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "Debe iniciar sesión como paciente."
            );
        }


        // =================================================
        // OBTENER DATOS
        // =================================================

        Long citaId;

        String numeroTarjeta;
        String titular;
        String vencimiento;
        String cvv;


        try {

            citaId =
                    Long.valueOf(
                            datos.get("citaId").toString()
                    );

            numeroTarjeta =
                    datos.get("numeroTarjeta")
                            .toString()
                            .replaceAll("\\s+", "");

            titular =
                    datos.get("titular")
                            .toString()
                            .trim();

            vencimiento =
                    datos.get("vencimiento")
                            .toString()
                            .trim();

            cvv =
                    datos.get("cvv")
                            .toString()
                            .trim();

        } catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "Los datos del pago están incompletos."
            );
        }


        // =================================================
        // VALIDACIONES BÁSICAS
        // =================================================

        if (!numeroTarjeta.matches("\\d{16}")) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El número de tarjeta debe contener 16 dígitos."
            );
        }


        if (titular.isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Debe ingresar el nombre del titular."
            );
        }


        if (!vencimiento.matches(
                "(0[1-9]|1[0-2])/\\d{2}"
        )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La fecha de vencimiento debe tener el formato MM/AA."
            );
        }


        if (!cvv.matches("\\d{3}")) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El CVV debe contener 3 dígitos."
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
                    "mensaje", "La cita no existe."
            );
        }


        // =================================================
        // COMPROBAR PROPIETARIO
        // =================================================

        if (cita.getPaciente() == null ||
                !nombreUsuario.equalsIgnoreCase(
                        cita.getPaciente().getNombreUsuario()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene autorización para pagar esta cita."
            );
        }


        // =================================================
        // COMPROBAR ESTADO
        // =================================================

        if (!"PENDIENTE_DE_PAGO"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Esta cita no se encuentra pendiente de pago."
            );
        }


        // =================================================
        // EVITAR PAGO DUPLICADO
        // =================================================

        if (pagoRepository.existsByCitaId(citaId)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Esta cita ya tiene un pago registrado."
            );
        }


        // =================================================
        // CREAR PAGO
        // =================================================

        Pago pago = new Pago();

        pago.setCita(cita);

        pago.setMonto(
                new BigDecimal("250.00")
        );

        pago.setMetodoPago(
                "TARJETA"
        );

        pago.setEstado(
                "APROBADO"
        );

        pago.setFechaPago(
                LocalDateTime.now()
        );


        // Generamos una referencia de transacción.
        // NO guardamos número de tarjeta ni CVV.

        String referencia =
                "HIS-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();


        pago.setReferencia(
                referencia
        );


        pagoRepository.save(pago);


        // =================================================
        // CONFIRMAR CITA
        // =================================================

        cita.setEstado(
                "CONFIRMADA"
        );

        citaRepository.save(cita);
        try {

            comprobanteService.enviarComprobante(
                    cita,
                    pago
            );

        } catch (Exception e) {

            System.out.println(
                    "No se pudo enviar el comprobante: "
                            + e.getMessage()
            );
        }

        // =================================================
        // RESPUESTA
        // =================================================

        return Map.of(
                "estado", "OK",

                "mensaje",
                "Pago realizado correctamente. Su cita ha sido confirmada.",

                "referencia",
                referencia,

                "citaId",
                cita.getId()
        );
    }
    @GetMapping("/paciente/pago/comprobante")
    public String mostrarComprobante(
            @RequestParam Long citaId,
            HttpSession session,
            Model model) {

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");


        // Verificar sesión del paciente
        if (nombreUsuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return "redirect:/login";
        }


        // Buscar la cita
        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElse(null);


        if (cita == null) {

            return "redirect:/paciente/dashboard";
        }


        // Verificar que la cita pertenezca
        // al paciente que inició sesión
        if (cita.getPaciente() == null ||
                !nombreUsuario.equalsIgnoreCase(
                        cita.getPaciente().getNombreUsuario()
                )) {

            return "redirect:/paciente/dashboard";
        }


        // Buscar pago asociado
        Pago pago =
                pagoRepository
                        .findByCitaId(citaId)
                        .orElse(null);


        if (pago == null) {

            return "redirect:/paciente/pago?citaId=" + citaId;
        }


        model.addAttribute(
                "cita",
                cita
        );

        model.addAttribute(
                "pago",
                pago
        );


        return "paciente/comprobante";
    }
}
