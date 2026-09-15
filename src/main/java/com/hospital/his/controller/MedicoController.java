package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.ConsultaMedica;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.ConsultaMedicaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Controller
public class MedicoController {


    private final CitaRepository citaRepository;

    private final ConsultaMedicaRepository
            consultaMedicaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public MedicoController(
            CitaRepository citaRepository,
            ConsultaMedicaRepository consultaMedicaRepository) {

        this.citaRepository =
                citaRepository;

        this.consultaMedicaRepository =
                consultaMedicaRepository;
    }


    // ==========================================
    // VERIFICAR SESIÓN DEL MÉDICO
    // ==========================================

    private boolean esMedico(
            HttpSession session) {

        String usuario =
                (String) session
                        .getAttribute("usuario");

        String rol =
                (String) session
                        .getAttribute("rol");


        return usuario != null
                && rol != null
                && "MEDICO"
                .equalsIgnoreCase(rol);
    }


    // ==========================================
    // VERIFICAR SI LA CITA ES DEL MÉDICO
    // ==========================================

    private boolean perteneceAlMedico(
            Cita cita,
            String nombreUsuario) {


        return cita.getMedico() != null

                && cita.getMedico()
                .getUsuario() != null

                && nombreUsuario
                .equalsIgnoreCase(

                        cita.getMedico()
                                .getUsuario()
                                .getNombreUsuario()
                );
    }


    // ==========================================
    // PANEL PRINCIPAL DEL MÉDICO
    // ==========================================

    @GetMapping("/medico")
    public String mostrarPanel(
            HttpSession session,
            Model model) {


        if (!esMedico(session)) {

            return "redirect:/medico/login";
        }


        String nombreUsuario =
                (String) session
                        .getAttribute("usuario");


        // ======================================
        // CITAS EN ESPERA
        // ======================================

        List<Cita> citasEnEspera =
                citaRepository
                        .findByEstadoOrderByFechaCreacionAsc(
                                "EN_ESPERA"
                        );


        List<Cita> citasEnEsperaDelMedico =
                citasEnEspera.stream()

                        .filter(cita ->

                                perteneceAlMedico(
                                        cita,
                                        nombreUsuario
                                )
                        )


                        // EMERGENCIAS PRIMERO

                        .sorted(

                                Comparator
                                        .comparingInt(

                                                (Cita cita) ->

                                                        "EMERGENCIA"
                                                                .equalsIgnoreCase(
                                                                        cita.getPrioridad()
                                                                )

                                                                ? 0
                                                                : 1
                                        )

                                        .thenComparing(

                                                cita ->

                                                        cita.getHoraLlegada()
                                                                == null

                                                                ? cita.getFechaCreacion()

                                                                : cita.getHoraLlegada()
                                        )
                        )

                        .toList();


        // ======================================
        // CITAS EN CONSULTA MÉDICA
        // ======================================

        List<Cita> citasEnConsulta =
                citaRepository
                        .findByEstadoOrderByFechaCreacionAsc(
                                "CONSULTA_MEDICA"
                        );


        List<Cita> citasEnConsultaDelMedico =
                citasEnConsulta.stream()

                        .filter(cita ->

                                perteneceAlMedico(
                                        cita,
                                        nombreUsuario
                                )
                        )

                        .toList();


        // ======================================
        // ENVIAR DATOS A LA VISTA
        // ======================================

        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );


        model.addAttribute(
                "citas",
                citasEnEsperaDelMedico
        );


        model.addAttribute(
                "citasEnConsulta",
                citasEnConsultaDelMedico
        );


        return "medico/index";
    }


    // ==========================================
    // INICIAR CONSULTA MÉDICA
    // CU-08 - PASO 2
    // ==========================================

    @PostMapping("/medico/cita/{id}/iniciar")
    public String iniciarConsulta(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {


        // VALIDAR SESIÓN

        if (!esMedico(session)) {

            return "redirect:/medico/login";
        }


        // BUSCAR CITA

        Optional<Cita> citaOptional =
                citaRepository.findById(id);


        if (citaOptional.isEmpty()) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "La cita seleccionada no existe."
                    );

            return "redirect:/medico";
        }


        Cita cita =
                citaOptional.get();


        // VALIDAR ESTADO

        if (!"EN_ESPERA"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "La cita no se encuentra en estado En Espera."
                    );

            return "redirect:/medico";
        }


        // VALIDAR MÉDICO

        String nombreUsuario =
                (String) session
                        .getAttribute("usuario");


        if (!perteneceAlMedico(
                cita,
                nombreUsuario
        )) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "No tiene autorización para atender esta cita."
                    );

            return "redirect:/medico";
        }


        // CAMBIAR ESTADO

        cita.setEstado(
                "CONSULTA_MEDICA"
        );


        citaRepository.save(cita);


        // MENSAJE

        redirectAttributes
                .addFlashAttribute(
                        "mensaje",
                        "Consulta iniciada correctamente para la cita #"
                                + cita.getId()
                );


        // LLAMADO POR VOZ

        redirectAttributes
                .addFlashAttribute(
                        "llamarPaciente",
                        true
                );


        redirectAttributes
                .addFlashAttribute(
                        "numeroTurno",
                        cita.getId()
                );


        if (cita.getPaciente() != null) {

            redirectAttributes
                    .addFlashAttribute(
                            "nombrePaciente",

                            cita.getPaciente()
                                    .getNombreCompleto()
                    );
        }


        return "redirect:/medico";
    }


    // ==========================================
    // VER / COMPLETAR CONSULTA
    // CU-08 - PASOS 3 Y 4
    // ==========================================

    @GetMapping("/medico/consulta/{id}")
    public String verConsulta(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {


        // VALIDAR SESIÓN

        if (!esMedico(session)) {

            return "redirect:/medico/login";
        }


        // BUSCAR CITA

        Optional<Cita> citaOptional =
                citaRepository.findById(id);


        if (citaOptional.isEmpty()) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "La cita seleccionada no existe."
                    );

            return "redirect:/medico";
        }


        Cita cita =
                citaOptional.get();


        // VALIDAR MÉDICO

        String nombreUsuario =
                (String) session
                        .getAttribute("usuario");


        if (!perteneceAlMedico(
                cita,
                nombreUsuario
        )) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "No tiene autorización para consultar esta cita."
                    );

            return "redirect:/medico";
        }


        // VALIDAR ESTADO

        if (!"CONSULTA_MEDICA"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "La cita no se encuentra en Consulta Médica."
                    );

            return "redirect:/medico";
        }


        // ======================================
        // BUSCAR CONSULTA EXISTENTE
        // ======================================

        Optional<ConsultaMedica>
                consultaOptional =
                consultaMedicaRepository
                        .findByCitaId(id);


        ConsultaMedica consulta =
                consultaOptional
                        .orElseGet(
                                ConsultaMedica::new
                        );


        // Si todavía no existe,
        // precargar motivo de la cita

        if (consulta.getId() == null) {

            consulta.setMotivoVisita(
                    cita.getMotivoConsulta()
            );

            consulta.setEstadoConsulta(
                    "EN_CURSO"
            );
        }


        // ENVIAR INFORMACIÓN

        model.addAttribute(
                "cita",
                cita
        );


        model.addAttribute(
                "consulta",
                consulta
        );


        model.addAttribute(
                "usuarioActual",
                nombreUsuario
        );


        return "medico/consulta";
    }


    // ==========================================
    // GUARDAR CONSULTA MÉDICA
    // CU-08 - PASOS 5 AL 9
    // ==========================================

    @PostMapping("/medico/consulta/{id}/guardar")
    public String guardarConsulta(
            @PathVariable Long id,

            @RequestParam String motivoVisita,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String hallazgosClinicos,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String codigoCie10,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String diagnostico,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String planTratamiento,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String notasAdicionales,

            @RequestParam String estadoConsulta,

            HttpSession session,

            RedirectAttributes redirectAttributes) {


        // ======================================
        // VALIDAR SESIÓN
        // ======================================

        if (!esMedico(session)) {

            return "redirect:/medico/login";
        }


        // ======================================
        // BUSCAR CITA
        // ======================================

        Optional<Cita> citaOptional =
                citaRepository.findById(id);


        if (citaOptional.isEmpty()) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "La cita seleccionada no existe."
                    );

            return "redirect:/medico";
        }


        Cita cita =
                citaOptional.get();


        // ======================================
        // VALIDAR MÉDICO
        // ======================================

        String nombreUsuario =
                (String) session
                        .getAttribute("usuario");


        if (!perteneceAlMedico(
                cita,
                nombreUsuario
        )) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "No tiene autorización para modificar esta consulta."
                    );

            return "redirect:/medico";
        }


        // ======================================
        // VALIDAR ESTADO DE LA CITA
        // ======================================

        if (!"CONSULTA_MEDICA"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "La cita no se encuentra en Consulta Médica."
                    );

            return "redirect:/medico";
        }


        // ======================================
        // VALIDAR MOTIVO DE VISITA
        // ======================================

        if (motivoVisita == null
                || motivoVisita.isBlank()) {

            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "El Motivo de Visita es obligatorio."
                    );

            return "redirect:/medico/consulta/"
                    + id;
        }


        // ======================================
        // FA05
        // FINALIZAR SIN DIAGNÓSTICO
        // ======================================

        if ("FINALIZADA"
                .equalsIgnoreCase(
                        estadoConsulta
                )

                &&

                (diagnostico == null
                        || diagnostico.isBlank())) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",

                            "No es posible finalizar la consulta sin registrar un diagnóstico. "
                                    + "El campo Diagnóstico es obligatorio."
                    );


            return "redirect:/medico/consulta/"
                    + id;
        }


        // ======================================
        // VALIDAR ESTADO DE CONSULTA
        // ======================================

        if (!"EN_CURSO"
                .equalsIgnoreCase(
                        estadoConsulta
                )

                &&

                !"FINALIZADA"
                        .equalsIgnoreCase(
                                estadoConsulta
                        )) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "El estado de consulta seleccionado no es válido."
                    );


            return "redirect:/medico/consulta/"
                    + id;
        }


        // ======================================
        // BUSCAR O CREAR CONSULTA
        // ======================================

        ConsultaMedica consulta =
                consultaMedicaRepository
                        .findByCitaId(id)

                        .orElseGet(
                                ConsultaMedica::new
                        );


        // ======================================
        // ASIGNAR DATOS
        // ======================================

        consulta.setCita(cita);

        consulta.setMotivoVisita(
                motivoVisita.trim()
        );

        consulta.setHallazgosClinicos(
                hallazgosClinicos
        );

        consulta.setCodigoCie10(
                codigoCie10
        );

        consulta.setDiagnostico(
                diagnostico
        );

        consulta.setPlanTratamiento(
                planTratamiento
        );

        consulta.setNotasAdicionales(
                notasAdicionales
        );

        consulta.setEstadoConsulta(
                estadoConsulta
                        .toUpperCase()
        );


        // ======================================
        // CONSULTA FINALIZADA
        // ======================================

        if ("FINALIZADA"
                .equalsIgnoreCase(
                        estadoConsulta
                )) {

            consulta.setFechaFinalizacion(
                    LocalDateTime.now()
            );
        }

        else {

            consulta.setFechaFinalizacion(
                    null
            );
        }


        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        consultaMedicaRepository
                .save(consulta);


        // ======================================
        // SI QUEDA EN CURSO
        // ======================================

        if ("EN_CURSO"
                .equalsIgnoreCase(
                        estadoConsulta
                )) {


            redirectAttributes
                    .addFlashAttribute(
                            "mensaje",

                            "La consulta ha sido guardada correctamente."
                    );


            return "redirect:/medico/consulta/"
                    + id;
        }


        // ======================================
        // SI SE FINALIZA LA CONSULTA
        // ======================================

        cita.setEstado(
                "EVALUADO"
        );


        citaRepository.save(cita);


        redirectAttributes
                .addFlashAttribute(
                        "mensaje",

                        "La consulta ha sido finalizada exitosamente. "
                                + "El paciente puede proceder a las siguientes indicaciones médicas."
                );


        return "redirect:/medico";
    }
}