package com.hospital.his.service;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.EventoAgendaMedica;
import com.hospital.his.entity.Medico;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.EventoAgendaMedicaRepository;
import com.hospital.his.repository.MedicoRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventoAgendaMedicaService {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final EventoAgendaMedicaRepository eventoRepository;

    private final MedicoRepository medicoRepository;

    private final CitaRepository citaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public EventoAgendaMedicaService(
            EventoAgendaMedicaRepository eventoRepository,
            MedicoRepository medicoRepository,
            CitaRepository citaRepository) {

        this.eventoRepository = eventoRepository;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
    }


    // ==========================================
    // OBTENER MÉDICO AUTENTICADO
    // ==========================================

    @Transactional(readOnly = true)
    public Medico obtenerMedicoAutenticado(
            HttpSession sesion) {

        if (sesion == null) {
            throw new SecurityException(
                    "Debe iniciar sesión."
            );
        }

        Object usuarioSesion =
                sesion.getAttribute("usuario");

        Object rolSesion =
                sesion.getAttribute("rol");

        if (!(usuarioSesion instanceof String nombreUsuario)
                || nombreUsuario.isBlank()
                || !(rolSesion instanceof String rol)
                || !"MEDICO".equalsIgnoreCase(rol)) {

            throw new SecurityException(
                    "Debe iniciar sesión como médico."
            );
        }

        Medico medico = medicoRepository
                .findByUsuarioNombreUsuarioAndActivoTrue(
                        nombreUsuario
                )
                .orElseThrow(() ->
                        new SecurityException(
                                "No existe un médico activo asociado a esta cuenta."
                        )
                );

        if (!Boolean.TRUE.equals(
                medico.getUsuario().getActivo()
        ) || !"MEDICO".equalsIgnoreCase(
                medico.getUsuario().getRol()
        )) {

            throw new SecurityException(
                    "El usuario no tiene permisos de médico."
            );
        }

        return medico;
    }


    // ==========================================
    // VALIDAR DATOS DEL EVENTO
    // ==========================================

    private void validarEvento(
            EventoAgendaMedica evento) {

        if (evento == null) {
            throw new IllegalArgumentException(
                    "Los datos del evento son obligatorios."
            );
        }

        if (evento.getTitulo() == null
                || evento.getTitulo().isBlank()) {

            throw new IllegalArgumentException(
                    "El título del evento es obligatorio."
            );
        }

        if (evento.getTitulo().length() > 200) {
            throw new IllegalArgumentException(
                    "El título no puede superar 200 caracteres."
            );
        }

        if (evento.getDescripcion() != null
                && evento.getDescripcion().length() > 500) {

            throw new IllegalArgumentException(
                    "La descripción no puede superar 500 caracteres."
            );
        }

        if (evento.getFechaInicio() == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria."
            );
        }

        if (evento.getFechaFin() == null) {
            throw new IllegalArgumentException(
                    "La fecha de fin es obligatoria."
            );
        }

        if (!evento.getFechaFin()
                .isAfter(evento.getFechaInicio())) {

            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio."
            );
        }

        if (evento.getTipoEvento() == null
                || evento.getTipoEvento() < 0
                || evento.getTipoEvento() > 4) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de evento válido."
            );
        }

        if (evento.getTodoElDia() == null) {
            evento.setTodoElDia(false);
        }
    }


    // ==========================================
    // CU-16: VALIDAR CONFLICTOS ENTRE EVENTOS
    // ==========================================

    private void validarConflictoEventos(
            Long medicoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long eventoExcluirId) {

        // Buscar eventos del médico que se superpongan
        // con el horario solicitado.

        List<EventoAgendaMedica> eventosCoincidentes =
                eventoRepository
                        .findByMedicoIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
                                medicoId,
                                fechaFin,
                                fechaInicio
                        );

        for (EventoAgendaMedica evento : eventosCoincidentes) {

            // Cuando se edita un evento, se excluye
            // el mismo evento de la comparación.

            if (eventoExcluirId != null
                    && eventoExcluirId.equals(evento.getId())) {

                continue;
            }

            throw new IllegalArgumentException(
                    "No se puede guardar el evento porque existe " +
                            "otro evento registrado en ese horario: " +
                            evento.getTitulo()
            );
        }
    }


    // ==========================================
    // CU-16: VALIDAR CONFLICTOS CON CITAS MÉDICAS
    // DURACIÓN DE CADA CITA: 30 MINUTOS
    // ==========================================

    private void validarConflictoCitas(
            Long medicoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        // Consultar las citas del médico autenticado.

        List<Cita> citas =
                citaRepository.buscarCitasAgendaMedica(
                        medicoId
                );

        for (Cita cita : citas) {

            // Solo considerar las citas confirmadas.

            if (!"CONFIRMADA".equalsIgnoreCase(
                    cita.getEstado()
            )) {
                continue;
            }

            // Verificar que exista un horario válido.

            if (cita.getHorario() == null
                    || cita.getHorario().getFecha() == null
                    || cita.getHorario().getHora() == null) {

                continue;
            }

            // Obtener la fecha y hora de inicio de la cita.

            LocalDateTime inicioCita =
                    LocalDateTime.of(
                            cita.getHorario().getFecha(),
                            cita.getHorario().getHora()
                    );

            // Cada cita médica dura 30 minutos.

            LocalDateTime finCita =
                    inicioCita.plusMinutes(30);

            // Detectar si el evento coincide
            // total o parcialmente con la cita.

            boolean existeConflicto =
                    fechaInicio.isBefore(finCita)
                            && fechaFin.isAfter(inicioCita);

            if (existeConflicto) {

                throw new IllegalArgumentException(
                        "No se puede guardar el evento porque existe " +
                                "una cita médica confirmada en ese horario. " +
                                "Fecha: " +
                                cita.getHorario().getFecha() +
                                ", hora: " +
                                cita.getHorario().getHora()
                );
            }
        }
    }


    // ==========================================
    // CREAR EVENTO
    // ==========================================

    @Transactional
    public EventoAgendaMedica crearEvento(
            EventoAgendaMedica solicitud,
            HttpSession sesion) {

        Medico medico =
                obtenerMedicoAutenticado(sesion);

        // Validar los datos ingresados.

        validarEvento(solicitud);

        // Validar conflictos con otros eventos.

        validarConflictoEventos(
                medico.getId(),
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                null
        );

        // Validar conflictos con citas médicas confirmadas.

        validarConflictoCitas(
                medico.getId(),
                solicitud.getFechaInicio(),
                solicitud.getFechaFin()
        );

        // Crear el nuevo evento.

        EventoAgendaMedica evento =
                new EventoAgendaMedica();

        evento.setMedico(medico);

        evento.setTitulo(
                solicitud.getTitulo().trim()
        );

        evento.setDescripcion(
                solicitud.getDescripcion()
        );

        evento.setFechaInicio(
                solicitud.getFechaInicio()
        );

        evento.setFechaFin(
                solicitud.getFechaFin()
        );

        evento.setTipoEvento(
                solicitud.getTipoEvento()
        );

        evento.setTodoElDia(
                solicitud.getTodoElDia()
        );

        return eventoRepository.save(evento);
    }


    // ==========================================
    // LISTAR EVENTOS DEL MÉDICO
    // ==========================================

    @Transactional(readOnly = true)
    public List<EventoAgendaMedica> listarEventos(
            HttpSession sesion) {

        Medico medico =
                obtenerMedicoAutenticado(sesion);

        return eventoRepository
                .findByMedicoIdOrderByFechaInicioAsc(
                        medico.getId()
                );
    }


    // ==========================================
    // LISTAR EVENTOS POR RANGO DE FECHAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<EventoAgendaMedica> listarPorFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            HttpSession sesion) {

        Medico medico =
                obtenerMedicoAutenticado(sesion);

        if (fechaInicio == null
                || fechaFin == null
                || !fechaFin.isAfter(fechaInicio)) {

            throw new IllegalArgumentException(
                    "El rango de fechas no es válido."
            );
        }

        return eventoRepository
                .findByMedicoIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
                        medico.getId(),
                        fechaFin,
                        fechaInicio
                );
    }


    // ==========================================
    // CONSULTAR EVENTO POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public EventoAgendaMedica obtenerEvento(
            Long eventoId,
            HttpSession sesion) {

        Medico medico =
                obtenerMedicoAutenticado(sesion);

        return eventoRepository
                .findByIdAndMedicoId(
                        eventoId,
                        medico.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El evento no existe o no pertenece al médico."
                        )
                );
    }


    // ==========================================
    // EDITAR EVENTO
    // ==========================================

    @Transactional
    public EventoAgendaMedica actualizarEvento(
            Long eventoId,
            EventoAgendaMedica solicitud,
            HttpSession sesion) {

        Medico medico =
                obtenerMedicoAutenticado(sesion);

        // Validar los datos ingresados.

        validarEvento(solicitud);

        // Comprobar que el evento pertenece
        // al médico autenticado.

        EventoAgendaMedica evento =
                eventoRepository
                        .findByIdAndMedicoId(
                                eventoId,
                                medico.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El evento no existe o no pertenece al médico."
                                )
                        );

        // Validar conflictos con otros eventos.
        // Excluir el evento que se está editando.

        validarConflictoEventos(
                medico.getId(),
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                eventoId
        );

        // Validar conflictos con citas médicas confirmadas.

        validarConflictoCitas(
                medico.getId(),
                solicitud.getFechaInicio(),
                solicitud.getFechaFin()
        );

        // Actualizar los datos del evento.

        evento.setTitulo(
                solicitud.getTitulo().trim()
        );

        evento.setDescripcion(
                solicitud.getDescripcion()
        );

        evento.setFechaInicio(
                solicitud.getFechaInicio()
        );

        evento.setFechaFin(
                solicitud.getFechaFin()
        );

        evento.setTipoEvento(
                solicitud.getTipoEvento()
        );

        evento.setTodoElDia(
                solicitud.getTodoElDia()
        );

        return eventoRepository.save(evento);
    }


    // ==========================================
    // ELIMINAR EVENTO
    // ==========================================

    @Transactional
    public void eliminarEvento(
            Long eventoId,
            HttpSession sesion) {

        Medico medico =
                obtenerMedicoAutenticado(sesion);

        EventoAgendaMedica evento =
                eventoRepository
                        .findByIdAndMedicoId(
                                eventoId,
                                medico.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El evento no existe o no pertenece al médico."
                                )
                        );

        eventoRepository.delete(evento);
    }
}