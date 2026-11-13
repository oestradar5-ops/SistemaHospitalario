package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.EventoAgendaMedica;
import com.hospital.his.entity.Medico;
import com.hospital.his.entity.TareaAgendaMedica;

import com.hospital.his.repository.CitaRepository;

import com.hospital.his.service.EventoAgendaMedicaService;
import com.hospital.his.service.TareaAgendaMedicaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/medico/agenda")
public class AgendaMedicaController {

    // ==========================================
    // SERVICIOS Y REPOSITORIOS
    // ==========================================

    private final EventoAgendaMedicaService eventoService;

    private final TareaAgendaMedicaService tareaService;

    private final CitaRepository citaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AgendaMedicaController(
            EventoAgendaMedicaService eventoService,
            TareaAgendaMedicaService tareaService,
            CitaRepository citaRepository
    ) {

        this.eventoService = eventoService;
        this.tareaService = tareaService;
        this.citaRepository = citaRepository;
    }


    // ==========================================
    // CU-16: ABRIR AGENDA MÉDICA
    // ==========================================

    @GetMapping
    public String mostrarAgenda(
            HttpSession sesion,
            Model model
    ) {

        try {

            // ======================================
            // IDENTIFICAR AL MÉDICO AUTENTICADO
            // ======================================

            Medico medico =
                    eventoService.obtenerMedicoAutenticado(sesion);


            // ======================================
            // CONSULTAR EVENTOS PERSONALES
            // ======================================

            List<EventoAgendaMedica> eventos =
                    eventoService.listarEventos(sesion);

            model.addAttribute(
                    "eventos",
                    eventos
            );


            // ======================================
            // CONSULTAR TAREAS
            // ======================================

            List<TareaAgendaMedica> tareas =
                    tareaService.listarTareas(sesion);

            model.addAttribute(
                    "tareas",
                    tareas
            );


            // ======================================
            // CU-16: RECORDATORIOS AUTOMÁTICOS
            // ======================================

            LocalDateTime ahora =
                    LocalDateTime.now();

            LocalDateTime limite =
                    ahora.plusHours(24);


            // ======================================
            // EVENTOS PRÓXIMOS
            // ======================================

            List<EventoAgendaMedica> eventosProximos =
                    eventos.stream()
                            .filter(evento ->
                                    evento.getFechaInicio() != null
                                            && !evento.getFechaInicio()
                                            .isBefore(ahora)
                                            && !evento.getFechaInicio()
                                            .isAfter(limite)
                            )
                            .collect(Collectors.toList());


            // ======================================
            // TAREAS PRÓXIMAS A VENCER
            // ======================================

            List<TareaAgendaMedica> tareasProximas =
                    tareas.stream()
                            .filter(tarea ->
                                    !Boolean.TRUE.equals(
                                            tarea.getIsCompleted()
                                    )
                                            && tarea.getFechaLimite() != null
                                            && !tarea.getFechaLimite()
                                            .isBefore(ahora)
                                            && !tarea.getFechaLimite()
                                            .isAfter(limite)
                            )
                            .collect(Collectors.toList());


            // ======================================
            // TAREAS VENCIDAS
            // ======================================

            List<TareaAgendaMedica> tareasVencidas =
                    tareas.stream()
                            .filter(tarea ->
                                    !Boolean.TRUE.equals(
                                            tarea.getIsCompleted()
                                    )
                                            && tarea.getFechaLimite() != null
                                            && tarea.getFechaLimite()
                                            .isBefore(ahora)
                            )
                            .collect(Collectors.toList());


            // ======================================
            // ENVIAR RECORDATORIOS A LA VISTA
            // ======================================

            model.addAttribute(
                    "eventosProximos",
                    eventosProximos
            );

            model.addAttribute(
                    "tareasProximas",
                    tareasProximas
            );

            model.addAttribute(
                    "tareasVencidas",
                    tareasVencidas
            );


            // ======================================
            // CONSULTAR CITAS DEL MÉDICO
            // ======================================

            List<Cita> citas =
                    citaRepository.buscarCitasAgendaMedica(
                            medico.getId()
                    );


            // ======================================
            // ENVIAR CITAS A LA VISTA
            // ======================================

            model.addAttribute(
                    "citasAgenda",
                    citas
            );


            // ======================================
            // MOSTRAR CALENDARIO
            // ======================================

            return "medico/agenda";

        } catch (RuntimeException e) {

            return "redirect:/medico/login";
        }
    }


    // ==========================================
    // CU-16: CREAR EVENTO
    // ==========================================

    @PostMapping("/eventos/crear")
    public String crearEvento(
            @RequestParam String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam Integer tipoEvento,
            @RequestParam(defaultValue = "false") Boolean todoElDia,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            // ======================================
            // CREAR OBJETO DEL EVENTO
            // ======================================

            EventoAgendaMedica evento =
                    new EventoAgendaMedica();

            evento.setTitulo(titulo);

            evento.setDescripcion(descripcion);

            evento.setFechaInicio(
                    LocalDateTime.parse(fechaInicio)
            );

            evento.setFechaFin(
                    LocalDateTime.parse(fechaFin)
            );

            evento.setTipoEvento(tipoEvento);

            evento.setTodoElDia(todoElDia);


            // ======================================
            // GUARDAR EVENTO EN POSTGRESQL
            // ======================================

            EventoAgendaMedica eventoGuardado =
                    eventoService.crearEvento(
                            evento,
                            sesion
                    );


            // ======================================
            // FORMATO DE FECHA DE GUATEMALA
            // ======================================

            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm"
                    );

            String inicioFormateado =
                    eventoGuardado
                            .getFechaInicio()
                            .format(formato);

            String finFormateado =
                    eventoGuardado
                            .getFechaFin()
                            .format(formato);


            // ======================================
            // MENSAJE DE CONFIRMACIÓN
            // ======================================

            String mensaje =
                    "Evento \"" +
                            eventoGuardado.getTitulo() +
                            "\" creado correctamente. " +
                            "Inicio: " +
                            inicioFormateado +
                            ". Finalización: " +
                            finFormateado +
                            ".";

            redirect.addFlashAttribute(
                    "mensaje",
                    mensaje
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: ACTUALIZAR EVENTO
    // ==========================================

    @PostMapping("/eventos/{id}/actualizar")
    public String actualizarEvento(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam Integer tipoEvento,
            @RequestParam(defaultValue = "false") Boolean todoElDia,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            EventoAgendaMedica evento =
                    new EventoAgendaMedica();

            evento.setTitulo(titulo);

            evento.setDescripcion(descripcion);

            evento.setFechaInicio(
                    LocalDateTime.parse(fechaInicio)
            );

            evento.setFechaFin(
                    LocalDateTime.parse(fechaFin)
            );

            evento.setTipoEvento(tipoEvento);

            evento.setTodoElDia(todoElDia);


            eventoService.actualizarEvento(
                    id,
                    evento,
                    sesion
            );


            redirect.addFlashAttribute(
                    "mensaje",
                    "Evento actualizado correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: ELIMINAR EVENTO
    // ==========================================

    @PostMapping("/eventos/{id}/eliminar")
    public String eliminarEvento(
            @PathVariable Long id,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            eventoService.eliminarEvento(
                    id,
                    sesion
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "Evento eliminado correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: CREAR TAREA
    // ==========================================

    @PostMapping("/tareas/crear")
    public String crearTarea(
            @RequestParam String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam Integer prioridad,
            @RequestParam String fechaLimite,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            TareaAgendaMedica tarea =
                    new TareaAgendaMedica();

            tarea.setTitulo(titulo);

            tarea.setDescripcion(descripcion);

            tarea.setPrioridad(prioridad);

            tarea.setFechaLimite(
                    LocalDateTime.parse(fechaLimite)
            );

            tarea.setIsCompleted(false);


            tareaService.crearTarea(
                    tarea,
                    sesion
            );


            redirect.addFlashAttribute(
                    "mensaje",
                    "Tarea creada correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: ACTUALIZAR TAREA
    // ==========================================

    @PostMapping("/tareas/{id}/actualizar")
    public String actualizarTarea(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam Integer prioridad,
            @RequestParam String fechaLimite,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            TareaAgendaMedica tarea =
                    new TareaAgendaMedica();

            tarea.setTitulo(titulo);

            tarea.setDescripcion(descripcion);

            tarea.setPrioridad(prioridad);

            tarea.setFechaLimite(
                    LocalDateTime.parse(fechaLimite)
            );


            tareaService.actualizarTarea(
                    id,
                    tarea,
                    sesion
            );


            redirect.addFlashAttribute(
                    "mensaje",
                    "Tarea actualizada correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: COMPLETAR TAREA
    // ==========================================

    @PostMapping("/tareas/{id}/completar")
    public String completarTarea(
            @PathVariable Long id,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            tareaService.cambiarEstado(
                    id,
                    true,
                    sesion
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "Tarea completada correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: REABRIR TAREA
    // ==========================================

    @PostMapping("/tareas/{id}/reabrir")
    public String reabrirTarea(
            @PathVariable Long id,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            tareaService.cambiarEstado(
                    id,
                    false,
                    sesion
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "Tarea reabierta correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }


    // ==========================================
    // CU-16: ELIMINAR TAREA
    // ==========================================

    @PostMapping("/tareas/{id}/eliminar")
    public String eliminarTarea(
            @PathVariable Long id,
            HttpSession sesion,
            RedirectAttributes redirect
    ) {

        try {

            tareaService.eliminarTarea(
                    id,
                    sesion
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "Tarea eliminada correctamente."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/medico/agenda";
    }
}