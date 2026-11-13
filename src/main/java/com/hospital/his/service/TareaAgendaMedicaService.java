package com.hospital.his.service;

import com.hospital.his.entity.Medico;
import com.hospital.his.entity.TareaAgendaMedica;

import com.hospital.his.repository.TareaAgendaMedicaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TareaAgendaMedicaService {

    // ==========================================
    // REPOSITORIO Y SERVICIO
    // ==========================================

    private final TareaAgendaMedicaRepository tareaRepository;

    private final EventoAgendaMedicaService eventoService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public TareaAgendaMedicaService(
            TareaAgendaMedicaRepository tareaRepository,
            EventoAgendaMedicaService eventoService) {

        this.tareaRepository = tareaRepository;
        this.eventoService = eventoService;
    }


    // ==========================================
    // OBTENER MÉDICO AUTENTICADO
    // ==========================================

    private Medico obtenerMedicoAutenticado(
            HttpSession sesion) {

        return eventoService.obtenerMedicoAutenticado(sesion);
    }


    // ==========================================
    // VALIDAR DATOS DE LA TAREA
    // ==========================================

    private void validarTarea(
            TareaAgendaMedica tarea) {

        if (tarea == null) {
            throw new IllegalArgumentException(
                    "Los datos de la tarea son obligatorios."
            );
        }

        if (tarea.getTitulo() == null
                || tarea.getTitulo().isBlank()) {

            throw new IllegalArgumentException(
                    "El título de la tarea es obligatorio."
            );
        }

        if (tarea.getTitulo().length() > 200) {
            throw new IllegalArgumentException(
                    "El título no puede superar 200 caracteres."
            );
        }

        if (tarea.getDescripcion() != null
                && tarea.getDescripcion().length() > 1000) {

            throw new IllegalArgumentException(
                    "La descripción no puede superar 1000 caracteres."
            );
        }

        if (tarea.getPrioridad() == null
                || tarea.getPrioridad() < 0
                || tarea.getPrioridad() > 2) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una prioridad válida."
            );
        }

        if (tarea.getFechaLimite() == null) {
            throw new IllegalArgumentException(
                    "La fecha límite es obligatoria."
            );
        }

        if (tarea.getIsCompleted() == null) {
            tarea.setIsCompleted(false);
        }
    }


    // ==========================================
    // CREAR TAREA
    // ==========================================

    @Transactional
    public TareaAgendaMedica crearTarea(
            TareaAgendaMedica solicitud,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        validarTarea(solicitud);

        TareaAgendaMedica tarea =
                new TareaAgendaMedica();

        tarea.setMedico(medico);

        tarea.setTitulo(
                solicitud.getTitulo().trim()
        );

        tarea.setDescripcion(
                solicitud.getDescripcion()
        );

        tarea.setPrioridad(
                solicitud.getPrioridad()
        );

        tarea.setFechaLimite(
                solicitud.getFechaLimite()
        );

        // Toda tarea nueva comienza pendiente
        tarea.setIsCompleted(false);

        return tareaRepository.save(tarea);
    }


    // ==========================================
    // LISTAR TODAS LAS TAREAS DEL MÉDICO
    // ==========================================

    @Transactional(readOnly = true)
    public List<TareaAgendaMedica> listarTareas(
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        return tareaRepository
                .findByMedicoIdOrderByFechaLimiteAsc(
                        medico.getId()
                );
    }


    // ==========================================
    // FILTRAR PENDIENTES O COMPLETADAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<TareaAgendaMedica> listarPorEstado(
            Boolean completada,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        if (completada == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el estado de las tareas."
            );
        }

        return tareaRepository
                .findByMedicoIdAndIsCompletedOrderByFechaLimiteAsc(
                        medico.getId(),
                        completada
                );
    }


    // ==========================================
    // CONSULTAR TAREAS POR FECHA
    // ==========================================

    @Transactional(readOnly = true)
    public List<TareaAgendaMedica> listarPorFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        if (fechaInicio == null
                || fechaFin == null
                || !fechaFin.isAfter(fechaInicio)) {

            throw new IllegalArgumentException(
                    "El rango de fechas no es válido."
            );
        }

        return tareaRepository
                .findByMedicoIdAndFechaLimiteGreaterThanEqualAndFechaLimiteLessThanOrderByFechaLimiteAsc(
                        medico.getId(),
                        fechaInicio,
                        fechaFin
                );
    }


    // ==========================================
    // CONSULTAR TAREA POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public TareaAgendaMedica obtenerTarea(
            Long tareaId,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        return tareaRepository
                .findByIdAndMedicoId(
                        tareaId,
                        medico.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La tarea no existe o no pertenece al médico."
                        )
                );
    }


    // ==========================================
    // EDITAR TAREA
    // ==========================================

    @Transactional
    public TareaAgendaMedica actualizarTarea(
            Long tareaId,
            TareaAgendaMedica solicitud,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        validarTarea(solicitud);

        TareaAgendaMedica tarea = tareaRepository
                .findByIdAndMedicoId(
                        tareaId,
                        medico.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La tarea no existe o no pertenece al médico."
                        )
                );

        tarea.setTitulo(
                solicitud.getTitulo().trim()
        );

        tarea.setDescripcion(
                solicitud.getDescripcion()
        );

        tarea.setPrioridad(
                solicitud.getPrioridad()
        );

        tarea.setFechaLimite(
                solicitud.getFechaLimite()
        );

        // Conservamos el estado actual de la tarea

        return tareaRepository.save(tarea);
    }


    // ==========================================
    // COMPLETAR O REABRIR TAREA
    // ==========================================

    @Transactional
    public TareaAgendaMedica cambiarEstado(
            Long tareaId,
            boolean completada,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        TareaAgendaMedica tarea = tareaRepository
                .findByIdAndMedicoId(
                        tareaId,
                        medico.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La tarea no existe o no pertenece al médico."
                        )
                );

        tarea.setIsCompleted(completada);

        return tareaRepository.save(tarea);
    }


    // ==========================================
    // ELIMINAR TAREA
    // ==========================================

    @Transactional
    public void eliminarTarea(
            Long tareaId,
            HttpSession sesion) {

        Medico medico = obtenerMedicoAutenticado(sesion);

        TareaAgendaMedica tarea = tareaRepository
                .findByIdAndMedicoId(
                        tareaId,
                        medico.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La tarea no existe o no pertenece al médico."
                        )
                );

        tareaRepository.delete(tarea);
    }
}