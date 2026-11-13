package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.Pago;
import com.hospital.his.entity.HorarioMedico;
import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.PagoRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import com.hospital.his.service.ComprobanteService;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class PagoController {

    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;
    private final ComprobanteService comprobanteService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String PENDIENTE = "PENDIENTE_DE_PAGO";
    private static final String EXPIRADA = "EXPIRADA";
    private static final int MINUTOS_RESERVA = 5;
    private static final String MENSAJE_EXPIRACION =
            "El tiempo para confirmar su cita ha expirado. " +
                    "El horario seleccionado ha sido liberado. " +
                    "Por favor, seleccione un nuevo horario.";

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
    @Transactional
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


        Cita cita = entityManager.find(
                Cita.class, citaId, LockModeType.PESSIMISTIC_WRITE);


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


        // Calculamos el tiempo desde la creación real de la cita,
        // sin reiniciarlo cuando se actualiza el navegador.
        if (PENDIENTE.equalsIgnoreCase(cita.getEstado())
                && segundosRestantes(cita) == 0L) {
            expirarReserva(cita);
        }

        boolean pendiente = PENDIENTE.equalsIgnoreCase(cita.getEstado());
        boolean expirada = EXPIRADA.equalsIgnoreCase(cita.getEstado());

        model.addAttribute("pendientePago", pendiente);
        model.addAttribute("reservaExpirada", expirada);
        model.addAttribute("segundosRestantes", pendiente ? segundosRestantes(cita) : 0L);

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
    // ESTADO DE RESERVA PARA EL RELOJ DE LA PÁGINA
    // =====================================================

    @ResponseBody
    @GetMapping("/api/pagos/reserva/estado")
    @Transactional
    public Map<String, Object> estadoReserva(
            @RequestParam Long citaId,
            HttpSession session) {

        String usuario = (String) session.getAttribute("usuario");
        String rol = (String) session.getAttribute("rol");

        if (usuario == null || !"PACIENTE".equalsIgnoreCase(rol)) {
            return Map.of("estado", "ERROR", "mensaje", "Debe iniciar sesión como paciente.");
        }

        Cita cita = entityManager.find(Cita.class, citaId, LockModeType.PESSIMISTIC_WRITE);
        if (cita == null || cita.getPaciente() == null ||
                !usuario.equalsIgnoreCase(cita.getPaciente().getNombreUsuario())) {
            return Map.of("estado", "ERROR", "mensaje", "Cita no disponible.");
        }

        if (PENDIENTE.equalsIgnoreCase(cita.getEstado()) && segundosRestantes(cita) == 0L) {
            expirarReserva(cita);
        }

        if (EXPIRADA.equalsIgnoreCase(cita.getEstado())) {
            return Map.of("estado", "EXPIRADA", "segundosRestantes", 0,
                    "mensaje", MENSAJE_EXPIRACION);
        }
        if (!PENDIENTE.equalsIgnoreCase(cita.getEstado())) {
            return Map.of("estado", "NO_DISPONIBLE", "segundosRestantes", 0,
                    "mensaje", "La cita ya no está pendiente de pago.");
        }
        return Map.of("estado", "ACTIVA", "segundosRestantes", segundosRestantes(cita));
    }

    // =====================================================
    // CUENTA REGRESIVA Y VENCIMIENTO DEL HORARIO
    // =====================================================

    private long segundosRestantes(Cita cita) {
        if (cita.getFechaCreacion() == null) {
            return 0L;
        }
        LocalDateTime vence = cita.getFechaCreacion().plusMinutes(MINUTOS_RESERVA);
        long milisegundos = Duration.between(LocalDateTime.now(), vence).toMillis();
        return Math.max(0L, (milisegundos + 999L) / 1000L);
    }

    private void expirarReserva(Cita cita) {
        // Solo se pueden expirar reservas aún pendientes; nunca citas pagadas.
        if (!PENDIENTE.equalsIgnoreCase(cita.getEstado()) || segundosRestantes(cita) > 0L) {
            return;
        }

        cita.setEstado(EXPIRADA);
        citaRepository.save(cita);

        if (cita.getHorario() == null) {
            return;
        }

        // No liberamos el horario si existe otra cita vigente asociada.
        Long otrosUsos = entityManager.createQuery(
                        "SELECT COUNT(c) FROM Cita c " +
                                "WHERE c.horario.id = :horarioId AND c.id <> :citaId " +
                                "AND c.estado NOT IN :estadosInactivos", Long.class)
                .setParameter("horarioId", cita.getHorario().getId())
                .setParameter("citaId", cita.getId())
                .setParameter("estadosInactivos", List.of("EXPIRADA", "CANCELADA", "CANCELADO"))
                .getSingleResult();

        if (otrosUsos == 0L) {
            HorarioMedico horario = entityManager.find(HorarioMedico.class,
                    cita.getHorario().getId(), LockModeType.PESSIMISTIC_WRITE);
            if (horario != null) {
                horario.setDisponible(true);
            }
        }
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

        Cita cita = entityManager.find(
                Cita.class, citaId, LockModeType.PESSIMISTIC_WRITE);


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


        // Si se venció la reserva, no se permite confirmar el pago.
        if (PENDIENTE.equalsIgnoreCase(cita.getEstado()) && segundosRestantes(cita) == 0L) {
            expirarReserva(cita);
            return Map.of("estado", "EXPIRADA", "mensaje", MENSAJE_EXPIRACION);
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
