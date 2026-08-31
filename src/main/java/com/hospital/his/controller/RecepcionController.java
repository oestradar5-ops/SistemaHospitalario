package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Cita;
import com.hospital.his.entity.Medico;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.MedicoRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Controller
public class RecepcionController {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final AuditoriaUsuarioRepository auditoriaUsuarioRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public RecepcionController(
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            MedicoRepository medicoRepository,
            AuditoriaUsuarioRepository auditoriaUsuarioRepository) {

        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.medicoRepository = medicoRepository;
        this.auditoriaUsuarioRepository = auditoriaUsuarioRepository;
    }


    // =====================================================
    // VALIDAR RECEPCIONISTA
    // =====================================================

    private boolean esRecepcionista(
            HttpSession session) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        return usuario != null
                && "RECEPCION".equalsIgnoreCase(rol);
    }


    // =====================================================
    // MOSTRAR RECEPCIÓN
    // =====================================================

    @GetMapping("/recepcion")
    public String mostrarRecepcion(
            HttpSession session,
            Model model) {

        if (!esRecepcionista(session)) {

            return "redirect:/recepcion/login";
        }


        model.addAttribute(
                "usuarioActual",
                session.getAttribute("usuario")
        );


        return "recepcion/index";
    }


    // =====================================================
    // BUSCAR POR NÚMERO DE CITA
    // =====================================================

    @ResponseBody
    @GetMapping("/api/recepcion/cita/{id}")
    public Map<String, Object> buscarPorCita(
            @PathVariable Long id,
            HttpSession session) {

        if (!esRecepcionista(session)) {

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


        if (cita == null) {

            return Map.of(
                    "estado", "NO_ENCONTRADA",
                    "mensaje",
                    "No se encontró ninguna cita con ese número."
            );
        }


        return construirRespuestaCita(cita);
    }


    // =====================================================
    // BUSCAR POR DPI
    // =====================================================

    @ResponseBody
    @GetMapping("/api/recepcion/dpi/{dpi}")
    public Map<String, Object> buscarPorDpi(
            @PathVariable String dpi,
            HttpSession session) {

        if (!esRecepcionista(session)) {

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


        if (paciente == null ||
                !"PACIENTE".equalsIgnoreCase(
                        paciente.getRol()
                )) {

            return Map.of(
                    "estado", "PACIENTE_NO_REGISTRADO",
                    "mensaje",
                    "No se encontró ningún paciente con ese DPI.",
                    "subtexto",
                    "Es necesario registrar al paciente antes de continuar."
            );
        }


        List<Cita> citas =
                citaRepository
                        .findByPacienteIdOrderByFechaCreacionDesc(
                                paciente.getId()
                        );


        if (citas.isEmpty()) {

            return Map.of(
                    "estado", "SIN_CITAS",
                    "mensaje",
                    "El paciente " +
                            paciente.getNombreCompleto() +
                            " está registrado pero no tiene citas activas.",
                    "subtexto",
                    "Puede crear una nueva cita para este paciente."
            );
        }


        Cita cita =
                citas.get(0);


        return construirRespuestaCita(cita);
    }


    // =====================================================
    // REGISTRAR LLEGADA
    // =====================================================

    @ResponseBody
    @PostMapping("/api/recepcion/citas/{id}/llegada")
    public Map<String, Object> registrarLlegada(
            @PathVariable Long id,
            HttpSession session) {

        if (!esRecepcionista(session)) {

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


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        if (!"CONFIRMADA".equalsIgnoreCase(
                cita.getEstado())) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Operación no permitida. " +
                            "La cita no se encuentra confirmada."
            );
        }


        // Cambiar estado

        cita.setEstado(
                "PACIENTE_PRESENTE"
        );


        // Registrar hora de llegada

        LocalDateTime horaLlegada =
                LocalDateTime.now();


        cita.setHoraLlegada(
                horaLlegada
        );


        // Guardar

        citaRepository.save(cita);


        String nombre =
                cita.getPaciente()
                        .getNombreCompleto();


        // =============================================
        // EMERGENCIA
        // =============================================

        if ("EMERGENCIA".equalsIgnoreCase(
                cita.getPrioridad())) {

            return Map.of(
                    "estado", "OK",

                    "mensaje",
                    "Paciente " +
                            nombre +
                            " registrado con prioridad de EMERGENCIA. " +
                            "El paciente debe pasar directamente " +
                            "a toma de signos vitales.",

                    "horaLlegada",
                    horaLlegada.toString()
            );
        }


        // =============================================
        // LLEGADA NORMAL
        // =============================================

        return Map.of(
                "estado", "OK",

                "mensaje",
                "La llegada del paciente " +
                        nombre +
                        " ha sido registrada exitosamente. " +
                        "El paciente debe pasar a la sala de espera.",

                "horaLlegada",
                horaLlegada.toString()
        );
    }


    // =====================================================
    // OBTENER MÉDICOS PARA REASIGNACIÓN
    // =====================================================

    @ResponseBody
    @GetMapping(
            "/api/recepcion/citas/{id}/medicos-disponibles"
    )
    public Map<String, Object> obtenerMedicosDisponibles(
            @PathVariable Long id,
            HttpSession session) {

        if (!esRecepcionista(session)) {

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


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        if (!"CONFIRMADA".equalsIgnoreCase(
                cita.getEstado()
        )
                &&
                !"PACIENTE_PRESENTE".equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no permite reasignación de médico."
            );
        }


        List<Medico> medicos =
                medicoRepository
                        .findBySucursalIdAndEspecialidadIdAndActivoTrue(
                                cita.getSucursal().getId(),
                                cita.getEspecialidad().getId()
                        );


        List<Map<String, Object>> disponibles =
                medicos.stream()

                        // Excluir al médico actual

                        .filter(
                                medico ->
                                        !medico
                                                .getId()
                                                .equals(
                                                        cita
                                                                .getMedico()
                                                                .getId()
                                                )
                        )

                        .map(
                                medico ->
                                        Map.<String, Object>of(

                                                "id",
                                                medico.getId(),

                                                "nombre",
                                                medico
                                                        .getUsuario()
                                                        .getNombreCompleto()
                                        )
                        )

                        .toList();


        return Map.of(
                "estado", "OK",

                "medicoActual",
                cita.getMedico()
                        .getUsuario()
                        .getNombreCompleto(),

                "medicos",
                disponibles
        );
    }


    // =====================================================
    // REASIGNAR MÉDICO
    // =====================================================

    @ResponseBody
    @PostMapping(
            "/api/recepcion/citas/{id}/reasignar-medico"
    )
    public Map<String, Object> reasignarMedico(
            @PathVariable Long id,
            @RequestBody Map<String, Object> datos,
            HttpSession session) {

        // =============================================
        // VALIDAR RECEPCIONISTA
        // =============================================

        if (!esRecepcionista(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        // =============================================
        // BUSCAR CITA
        // =============================================

        Cita cita =
                citaRepository
                        .findById(id)
                        .orElse(null);


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        // =============================================
        // VALIDAR ESTADO
        // =============================================

        if (!"CONFIRMADA".equalsIgnoreCase(
                cita.getEstado()
        )
                &&
                !"PACIENTE_PRESENTE".equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no permite reasignación de médico."
            );
        }


        // =============================================
        // OBTENER ID DEL NUEVO MÉDICO
        // =============================================

        Long nuevoMedicoId;


        try {

            Object medicoId =
                    datos.get("medicoId");


            if (medicoId == null) {

                return Map.of(
                        "estado", "ERROR",
                        "mensaje",
                        "Debe seleccionar un médico."
                );
            }


            nuevoMedicoId =
                    Long.valueOf(
                            medicoId.toString()
                    );

        }

        catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Debe seleccionar un médico válido."
            );
        }


        // =============================================
        // BUSCAR NUEVO MÉDICO
        // =============================================

        Medico nuevoMedico =
                medicoRepository
                        .findById(nuevoMedicoId)
                        .orElse(null);


        if (nuevoMedico == null ||
                !Boolean.TRUE.equals(
                        nuevoMedico.getActivo()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El médico seleccionado no está disponible."
            );
        }


        // =============================================
        // VALIDAR MISMA SUCURSAL
        // =============================================

        if (!nuevoMedico
                .getSucursal()
                .getId()
                .equals(
                        cita.getSucursal()
                                .getId()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El médico no pertenece a la misma sucursal."
            );
        }


        // =============================================
        // VALIDAR MISMA ESPECIALIDAD
        // =============================================

        if (!nuevoMedico
                .getEspecialidad()
                .getId()
                .equals(
                        cita.getEspecialidad()
                                .getId()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El médico no pertenece a la misma especialidad."
            );
        }


        // =============================================
        // VALIDAR QUE NO SEA EL MISMO
        // =============================================

        if (nuevoMedico
                .getId()
                .equals(
                        cita.getMedico()
                                .getId()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El médico seleccionado ya está asignado a esta cita."
            );
        }


        // =============================================
        // GUARDAR DATOS ANTERIORES
        // =============================================

        String medicoAnterior =
                cita.getMedico()
                        .getUsuario()
                        .getNombreCompleto();


        String medicoNuevo =
                nuevoMedico
                        .getUsuario()
                        .getNombreCompleto();


        // =============================================
        // OBTENER NOTA
        // =============================================

        String nota =
                datos.get("nota") == null
                        ? ""
                        : datos.get("nota")
                        .toString()
                        .trim();


        // =============================================
        // REASIGNAR MÉDICO
        // =============================================

        cita.setMedico(
                nuevoMedico
        );


        citaRepository.save(cita);


        // =============================================
        // REGISTRAR AUDITORÍA
        // =============================================

        AuditoriaUsuario auditoria =
                new AuditoriaUsuario();


        auditoria.setAdministrador(
                (String) session.getAttribute(
                        "usuario"
                )
        );


        auditoria.setAccion(
                "REASIGNACION_MEDICO"
        );


        auditoria.setUsuarioAfectado(
                cita.getPaciente()
                        .getNombreUsuario()
        );


        String descripcion =
                "Cita No. " +
                        cita.getId() +
                        ". Médico anterior: " +
                        medicoAnterior +
                        ". Nuevo médico: " +
                        medicoNuevo;


        if (!nota.isBlank()) {

            descripcion +=
                    ". Motivo: " +
                            nota;
        }


        // Evitar exceder VARCHAR(255)

        if (descripcion.length() > 255) {

            descripcion =
                    descripcion.substring(
                            0,
                            255
                    );
        }


        auditoria.setDescripcion(
                descripcion
        );


        auditoria.setFechaHora(
                LocalDateTime.now()
        );


        auditoriaUsuarioRepository.save(
                auditoria
        );


        // =============================================
        // CONSOLA
        // =============================================

        System.out.println(
                "REASIGNACIÓN DE MÉDICO - " +
                        "Cita: " +
                        cita.getId() +
                        " | Anterior: " +
                        medicoAnterior +
                        " | Nuevo: " +
                        medicoNuevo +
                        " | Nota: " +
                        nota
        );


        // =============================================
        // RESPUESTA
        // =============================================

        return Map.of(
                "estado", "OK",

                "mensaje",
                "Médico reasignado correctamente.",

                "medico",
                medicoNuevo
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
                        "estadoCita",
                        cita.getEstado()
                ),

                Map.entry(
                        "prioridad",
                        cita.getPrioridad() == null
                                ? "NORMAL"
                                : cita.getPrioridad()
                ),

                Map.entry(
                        "especialidad",
                        cita.getEspecialidad()
                                .getNombre()
                ),

                Map.entry(
                        "sucursal",
                        cita.getSucursal()
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
                        "motivo",
                        cita.getMotivoConsulta() == null
                                ? ""
                                : cita.getMotivoConsulta()
                ),

                Map.entry(
                        "horaLlegada",
                        cita.getHoraLlegada() == null
                                ? ""
                                : cita.getHoraLlegada()
                                .toString()
                )
        );
    }
}