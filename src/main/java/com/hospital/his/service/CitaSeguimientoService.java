package com.hospital.his.service;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.ConsultaMedica;
import com.hospital.his.entity.HorarioMedico;

import com.hospital.his.event.CitaSeguimientoRegistradaEvent;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.ConsultaMedicaRepository;
import com.hospital.his.repository.HorarioMedicoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class CitaSeguimientoService {

    // ==========================================
    // REPOSITORIOS Y EVENTOS
    // ==========================================

    private final CitaRepository citaRepository;

    private final ConsultaMedicaRepository consultaMedicaRepository;

    private final HorarioMedicoRepository horarioMedicoRepository;

    private final ApplicationEventPublisher eventPublisher;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CitaSeguimientoService(
            CitaRepository citaRepository,
            ConsultaMedicaRepository consultaMedicaRepository,
            HorarioMedicoRepository horarioMedicoRepository,
            ApplicationEventPublisher eventPublisher) {

        this.citaRepository = citaRepository;

        this.consultaMedicaRepository = consultaMedicaRepository;

        this.horarioMedicoRepository = horarioMedicoRepository;

        this.eventPublisher = eventPublisher;
    }

    // ==========================================
    // CU-12 - AGENDAR CITA DE SEGUIMIENTO
    // ==========================================

    @Transactional
    public Cita agendarSeguimiento(
            Long consultaOrigenId,
            Long horarioId,
            String tipoSeguimiento,
            String motivo,
            String prioridad,
            String nombreUsuarioMedico) {

        // ======================================
        // VALIDAR DATOS OBLIGATORIOS
        // ======================================

        if (consultaOrigenId == null || horarioId == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una consulta y un horario."
            );
        }

        if (nombreUsuarioMedico == null
                || nombreUsuarioMedico.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe iniciar sesión como médico."
            );
        }

        // ======================================
        // BUSCAR CONSULTA ORIGINAL
        // ======================================

        ConsultaMedica consulta =
                consultaMedicaRepository
                        .findById(consultaOrigenId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La consulta médica original no existe."
                                )
                        );

        // ======================================
        // VALIDAR ESTADO DE LA CONSULTA
        // CU-12: EN_CURSO O FINALIZADA
        // ======================================

        String estadoConsulta = consulta.getEstadoConsulta();

        if (estadoConsulta == null
                || (!"EN_CURSO".equalsIgnoreCase(estadoConsulta)
                && !"FINALIZADA".equalsIgnoreCase(estadoConsulta))) {

            throw new IllegalArgumentException(
                    "La consulta médica debe estar en curso o finalizada."
            );
        }

        // ======================================
        // VALIDAR CITA ORIGINAL
        // ======================================

        Cita citaOriginal = consulta.getCita();

        if (citaOriginal == null
                || citaOriginal.getMedico() == null
                || citaOriginal.getMedico().getUsuario() == null) {

            throw new IllegalArgumentException(
                    "La consulta original no tiene un médico válido."
            );
        }

        // ======================================
        // VALIDAR MÉDICO RESPONSABLE
        // ======================================

        String usuarioResponsable =
                citaOriginal.getMedico()
                        .getUsuario()
                        .getNombreUsuario();

        if (usuarioResponsable == null
                || !nombreUsuarioMedico.equalsIgnoreCase(
                usuarioResponsable)) {

            throw new IllegalArgumentException(
                    "No tiene autorización para agendar "
                            + "seguimientos de esta consulta."
            );
        }

        // ======================================
        // VALIDAR DATOS DE LA CITA ORIGINAL
        // ======================================

        if (citaOriginal.getPaciente() == null
                || citaOriginal.getSucursal() == null
                || citaOriginal.getEspecialidad() == null) {

            throw new IllegalArgumentException(
                    "La cita original no tiene todos los datos necesarios "
                            + "para agendar el seguimiento."
            );
        }

        // ======================================
        // VALIDAR TIPO DE SEGUIMIENTO
        // ======================================

        if (!"MONITOREO_TRATAMIENTO".equals(tipoSeguimiento)
                && !"REVISION_RESULTADOS_LABORATORIO".equals(tipoSeguimiento)) {

            throw new IllegalArgumentException(
                    "El tipo de seguimiento seleccionado no es válido."
            );
        }

        // ======================================
        // VALIDAR MOTIVO
        // ======================================

        if (motivo == null
                || motivo.trim().length() < 10
                || motivo.trim().length() > 2000) {

            throw new IllegalArgumentException(
                    "El motivo debe contener entre 10 y 2000 caracteres."
            );
        }

        // ======================================
        // VALIDAR PRIORIDAD
        // ======================================

        if (prioridad == null
                || (!"NORMAL".equalsIgnoreCase(prioridad)
                && !"EMERGENCIA".equalsIgnoreCase(prioridad))) {

            throw new IllegalArgumentException(
                    "La prioridad seleccionada no es válida."
            );
        }

        // ======================================
        // BLOQUEAR HORARIO
        // EVITAR RESERVAS SIMULTÁNEAS
        // ======================================

        HorarioMedico horario =
                horarioMedicoRepository
                        .buscarHorarioParaReserva(horarioId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El horario seleccionado no existe."
                                )
                        );

        // ======================================
        // VALIDAR MÉDICO DEL HORARIO
        // ======================================

        if (horario.getMedico() == null
                || horario.getMedico().getId() == null
                || citaOriginal.getMedico().getId() == null
                || !horario.getMedico().getId()
                .equals(citaOriginal.getMedico().getId())) {

            throw new IllegalArgumentException(
                    "El horario no pertenece al médico responsable."
            );
        }

        // ======================================
        // VALIDAR FECHA Y HORA
        // ======================================

        if (horario.getFecha() == null
                || horario.getHora() == null) {

            throw new IllegalArgumentException(
                    "El horario seleccionado no tiene fecha u hora válida."
            );
        }

        LocalDateTime fechaHora =
                LocalDateTime.of(
                        horario.getFecha(),
                        horario.getHora()
                );

        if (!fechaHora.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una fecha y hora futuras."
            );
        }

        // ======================================
        // VALIDAR DISPONIBILIDAD
        // FA01 - CONFLICTO DE HORARIO
        // ======================================

        if (!Boolean.TRUE.equals(horario.getDisponible())
                || citaRepository.existsByHorarioId(horarioId)) {

            throw new IllegalArgumentException(
                    "El horario seleccionado ya no está disponible. "
                            + "Por favor, elija otro horario."
            );
        }

        // ======================================
        // CREAR CITA DE SEGUIMIENTO
        // ======================================

        Cita nuevaCita = new Cita();

        // PACIENTE ORIGINAL

        nuevaCita.setPaciente(
                citaOriginal.getPaciente()
        );

        // MÉDICO RESPONSABLE

        nuevaCita.setMedico(
                citaOriginal.getMedico()
        );

        // SUCURSAL ORIGINAL

        nuevaCita.setSucursal(
                citaOriginal.getSucursal()
        );

        // ESPECIALIDAD ORIGINAL

        nuevaCita.setEspecialidad(
                citaOriginal.getEspecialidad()
        );

        // HORARIO SELECCIONADO

        nuevaCita.setHorario(horario);

        // MOTIVO DEL SEGUIMIENTO

        nuevaCita.setMotivoConsulta(
                motivo.trim()
        );

        // PRIORIDAD

        nuevaCita.setPrioridad(
                prioridad.toUpperCase(Locale.ROOT)
        );

        // ======================================
        // ESTADO INICIAL DE LA CITA
        // ======================================

        // Se conserva el flujo de pago existente.
        // Este comportamiento debe validarse con
        // las reglas generales del hospital.

        nuevaCita.setEstado(
                "PENDIENTE_DE_PAGO"
        );

        // ======================================
        // IDENTIFICAR CITA DE SEGUIMIENTO
        // ======================================

        nuevaCita.setOrigenCita(
                "SEGUIMIENTO"
        );

        // ======================================
        // RELACIONAR CONSULTA ORIGINAL
        // ======================================

        nuevaCita.setConsultaOrigen(
                consulta
        );

        // ======================================
        // TIPO DE SEGUIMIENTO
        // ======================================

        nuevaCita.setTipoSeguimiento(
                tipoSeguimiento
        );

        // ======================================
        // RECORDATORIO PENDIENTE
        // ======================================

        nuevaCita.setRecordatorioSeguimientoEnviado(false);

        // ======================================
        // GUARDAR CITA EN POSTGRESQL
        // ======================================

        Cita citaGuardada =
                citaRepository.save(nuevaCita);

        // ======================================
        // RESERVAR HORARIO
        // ======================================

        horario.setDisponible(false);

        horarioMedicoRepository.save(horario);

        // ======================================
        // PUBLICAR EVENTO DE CONFIRMACIÓN
        // ======================================

        eventPublisher.publishEvent(
                new CitaSeguimientoRegistradaEvent(
                        citaGuardada.getId()
                )
        );

        // ======================================
        // DEVOLVER CITA REGISTRADA
        // ======================================

        return citaGuardada;
    }
}