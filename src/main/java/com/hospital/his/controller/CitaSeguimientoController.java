package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.ConsultaMedica;

import com.hospital.his.repository.ConsultaMedicaRepository;
import com.hospital.his.repository.HorarioMedicoRepository;

import com.hospital.his.service.CitaSeguimientoService;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/medico/citas-seguimiento")
public class CitaSeguimientoController {

    // ==========================================
    // LOGGER
    // ==========================================

    private static final Logger log =
            LoggerFactory.getLogger(
                    CitaSeguimientoController.class
            );

    // ==========================================
    // REPOSITORIOS Y SERVICIOS
    // ==========================================

    private final ConsultaMedicaRepository consultaMedicaRepository;

    private final HorarioMedicoRepository horarioMedicoRepository;

    private final CitaSeguimientoService citaSeguimientoService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CitaSeguimientoController(
            ConsultaMedicaRepository consultaMedicaRepository,
            HorarioMedicoRepository horarioMedicoRepository,
            CitaSeguimientoService citaSeguimientoService) {

        this.consultaMedicaRepository = consultaMedicaRepository;

        this.horarioMedicoRepository = horarioMedicoRepository;

        this.citaSeguimientoService = citaSeguimientoService;
    }

    // ==========================================
    // VALIDAR SESIÓN DEL MÉDICO
    // ==========================================

    private boolean esMedico(HttpSession session) {

        Object usuario = session.getAttribute("usuario");

        Object rol = session.getAttribute("rol");

        return usuario instanceof String
                && !((String) usuario).isBlank()
                && rol instanceof String
                && "MEDICO".equalsIgnoreCase((String) rol);
    }

    // ==========================================
    // VALIDAR MÉDICO RESPONSABLE
    // ==========================================

    private boolean perteneceAlMedico(
            ConsultaMedica consulta,
            String nombreUsuario) {

        if (consulta == null
                || nombreUsuario == null
                || nombreUsuario.isBlank()) {

            return false;
        }

        return consulta.getCita() != null
                && consulta.getCita().getMedico() != null
                && consulta.getCita().getMedico().getUsuario() != null
                && nombreUsuario.equalsIgnoreCase(
                consulta.getCita()
                        .getMedico()
                        .getUsuario()
                        .getNombreUsuario()
        );
    }

    // ==========================================
    // CU-12 - VALIDAR ESTADO DE CONSULTA
    // ==========================================

    private boolean estadoConsultaPermitido(
            ConsultaMedica consulta) {

        if (consulta == null
                || consulta.getEstadoConsulta() == null) {

            return false;
        }

        String estado = consulta.getEstadoConsulta();

        return "EN_CURSO".equalsIgnoreCase(estado)
                || "FINALIZADA".equalsIgnoreCase(estado);
    }

    // ==========================================
    // CU-12 - MOSTRAR FORMULARIO
    // ==========================================

    @GetMapping("/nueva/{consultaId}")
    public String mostrarFormulario(
            @PathVariable Long consultaId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {

            return "redirect:/medico/login";
        }

        // ======================================
        // BUSCAR CONSULTA ORIGINAL
        // ======================================

        ConsultaMedica consulta =
                consultaMedicaRepository
                        .findById(consultaId)
                        .orElse(null);

        if (consulta == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró la consulta médica original."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR MÉDICO RESPONSABLE
        // ======================================

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(
                consulta,
                nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para agendar "
                            + "seguimientos de esta consulta."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR ESTADO DE CONSULTA
        // EN_CURSO O FINALIZADA
        // ======================================

        if (!estadoConsultaPermitido(consulta)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La consulta médica debe estar en curso o finalizada."
            );

            return "redirect:/medico";
        }

        // ======================================
        // OBTENER CITA ORIGINAL
        // ======================================

        Cita citaOriginal = consulta.getCita();

        if (citaOriginal == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La consulta médica no tiene una cita asociada."
            );

            return "redirect:/medico";
        }

        // ======================================
        // ENVIAR DATOS AL FORMULARIO
        // ======================================

        model.addAttribute(
                "consulta",
                consulta
        );

        model.addAttribute(
                "citaOriginal",
                citaOriginal
        );

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );

        // ======================================
        // IDENTIFICAR ESTADO DE CONSULTA
        // ======================================

        model.addAttribute(
                "consultaEnCurso",
                "EN_CURSO".equalsIgnoreCase(
                        consulta.getEstadoConsulta()
                )
        );

        // ======================================
        // RUTA PARA REGRESAR
        // ======================================

        String urlVolver;

        if ("EN_CURSO".equalsIgnoreCase(
                consulta.getEstadoConsulta())) {

            urlVolver =
                    "/medico/consulta/" + citaOriginal.getId();

        } else {

            urlVolver = "/medico";
        }

        model.addAttribute(
                "urlVolver",
                urlVolver
        );

        // ======================================
        // MOSTRAR FORMULARIO CU-12
        // ======================================

        return "medico/cita-seguimiento";
    }

    // ==========================================
    // CU-12 - CONSULTAR HORARIOS DISPONIBLES
    // ==========================================

    @ResponseBody
    @GetMapping("/api/horarios/{consultaId}")
    public List<Map<String, Object>> obtenerHorarios(
            @PathVariable Long consultaId,
            HttpSession session) {

        // ======================================
        // VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {

            return List.of();
        }

        // ======================================
        // BUSCAR CONSULTA ORIGINAL
        // ======================================

        ConsultaMedica consulta =
                consultaMedicaRepository
                        .findById(consultaId)
                        .orElse(null);

        if (consulta == null) {

            return List.of();
        }

        // ======================================
        // VALIDAR MÉDICO RESPONSABLE
        // ======================================

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(
                consulta,
                nombreUsuario)) {

            return List.of();
        }

        // ======================================
        // VALIDAR ESTADO DE CONSULTA
        // ======================================

        if (!estadoConsultaPermitido(consulta)) {

            return List.of();
        }

        // ======================================
        // OBTENER MÉDICO RESPONSABLE
        // ======================================

        Long medicoId =
                consulta.getCita()
                        .getMedico()
                        .getId();

        if (medicoId == null) {

            return List.of();
        }

        // ======================================
        // FECHA Y HORA ACTUALES
        // ======================================

        LocalDateTime ahora = LocalDateTime.now();

        // ======================================
        // CONSULTAR HORARIOS DISPONIBLES
        // ======================================

        return horarioMedicoRepository
                .findByMedicoIdAndDisponibleTrueOrderByFechaAscHoraAsc(
                        medicoId
                )
                .stream()

                // VALIDAR FECHA Y HORA

                .filter(horario ->
                        horario.getFecha() != null
                                && horario.getHora() != null
                )

                // MOSTRAR SOLO HORARIOS FUTUROS

                .filter(horario ->
                        LocalDateTime.of(
                                horario.getFecha(),
                                horario.getHora()
                        ).isAfter(ahora)
                )

                // PREPARAR RESPUESTA JSON

                .map(horario -> Map.<String, Object>of(
                        "id", horario.getId(),
                        "fecha", horario.getFecha().toString(),
                        "hora", horario.getHora().toString()
                ))

                .toList();
    }

    // ==========================================
    // CU-12 - CONFIRMAR CITA DE SEGUIMIENTO
    // ==========================================

    @ResponseBody
    @PostMapping("/api/confirmar")
    public Map<String, Object> confirmarSeguimiento(
            @RequestBody(required = false) Map<String, Object> datos,
            HttpSession session) {

        // ======================================
        // VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", "Debe iniciar sesión como médico."
            );
        }

        try {

            // ==================================
            // VALIDAR DATOS RECIBIDOS
            // ==================================

            if (datos == null
                    || datos.get("consultaId") == null
                    || datos.get("horarioId") == null
                    || datos.get("tipoSeguimiento") == null
                    || datos.get("motivo") == null
                    || datos.get("prioridad") == null) {

                return Map.of(
                        "estado", "ERROR",
                        "mensaje", "Debe completar todos los campos."
                );
            }

            // ==================================
            // OBTENER IDENTIFICADORES
            // ==================================

            Long consultaId =
                    Long.valueOf(
                            datos.get("consultaId").toString()
                    );

            Long horarioId =
                    Long.valueOf(
                            datos.get("horarioId").toString()
                    );

            // ==================================
            // OBTENER TIPO DE SEGUIMIENTO
            // ==================================

            String tipoSeguimiento =
                    datos.get("tipoSeguimiento").toString();

            // ==================================
            // OBTENER MOTIVO
            // ==================================

            String motivo =
                    datos.get("motivo").toString();

            // ==================================
            // OBTENER PRIORIDAD
            // ==================================

            String prioridad =
                    datos.get("prioridad").toString();

            // ==================================
            // OBTENER MÉDICO DE LA SESIÓN
            // ==================================

            String nombreUsuario =
                    (String) session.getAttribute("usuario");

            // ==================================
            // REGISTRAR CITA DE SEGUIMIENTO
            // ==================================

            Cita cita =
                    citaSeguimientoService.agendarSeguimiento(
                            consultaId,
                            horarioId,
                            tipoSeguimiento,
                            motivo,
                            prioridad,
                            nombreUsuario
                    );

            // ==================================
            // OBTENER NOMBRE DEL PACIENTE
            // ==================================

            String nombrePaciente = "Paciente";

            if (cita.getPaciente() != null
                    && cita.getPaciente().getNombreCompleto() != null) {

                nombrePaciente =
                        cita.getPaciente().getNombreCompleto();
            }

            // ==================================
            // PREPARAR NOMBRE DEL SEGUIMIENTO
            // ==================================

            String tipoDescripcion;

            if ("MONITOREO_TRATAMIENTO".equals(
                    tipoSeguimiento)) {

                tipoDescripcion =
                        "Monitoreo de Tratamiento";

            } else {

                tipoDescripcion =
                        "Revisión de Resultados de Laboratorio";
            }

            // ==================================
            // MENSAJE DE CONFIRMACIÓN
            // ==================================

            String mensaje =
                    "Cita de seguimiento agendada exitosamente. "
                            + "Tipo: " + tipoDescripcion
                            + ". Paciente: " + nombrePaciente + ".";

            // ==================================
            // REGISTRAR EVENTO EN LOG
            // ==================================

            log.info(
                    "CU-12: Cita de seguimiento {} registrada por {}.",
                    cita.getId(),
                    nombreUsuario
            );

            // ==================================
            // RESPUESTA EXITOSA
            // ==================================

            return Map.of(
                    "estado", "OK",
                    "mensaje", mensaje,
                    "citaId", cita.getId(),
                    "tipoSeguimiento", tipoDescripcion,
                    "paciente", nombrePaciente
            );

        } catch (IllegalArgumentException e) {

            // ==================================
            // ERRORES DE VALIDACIÓN
            // ==================================

            return Map.of(
                    "estado", "ERROR",
                    "mensaje", e.getMessage()
            );

        } catch (Exception e) {

            // ==================================
            // ERRORES INTERNOS
            // ==================================

            log.error(
                    "CU-12: Error al registrar una cita de seguimiento.",
                    e
            );

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No fue posible registrar la cita de seguimiento."
            );
        }
    }

    // ==========================================
    // CU-12 - ABRIR SEGUIMIENTO DESDE UNA CITA
    // ==========================================

    @GetMapping("/nueva-desde-cita/{citaId}")
    public String nuevaDesdeCita(
            @PathVariable Long citaId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {

            return "redirect:/medico/login";
        }

        // ======================================
        // BUSCAR CONSULTA POR CITA
        // ======================================

        ConsultaMedica consulta =
                consultaMedicaRepository
                        .findByCitaId(citaId)
                        .orElse(null);

        if (consulta == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró la consulta médica de esta cita."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR MÉDICO RESPONSABLE
        // ======================================

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        if (!perteneceAlMedico(
                consulta,
                nombreUsuario)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene autorización para agendar este seguimiento."
            );

            return "redirect:/medico";
        }

        // ======================================
        // VALIDAR ESTADO DE CONSULTA
        // ======================================

        if (!estadoConsultaPermitido(consulta)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La consulta médica debe estar en curso o finalizada."
            );

            return "redirect:/medico";
        }

        // ======================================
        // ABRIR FORMULARIO DE SEGUIMIENTO
        // ======================================

        return "redirect:/medico/citas-seguimiento/nueva/"
                + consulta.getId();
    }
}