package com.hospital.his.service;

import com.hospital.his.entity.Cita;
import com.hospital.his.repository.CitaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordatorioSeguimientoService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RecordatorioSeguimientoService.class
            );

    private final CitaRepository citaRepository;
    private final CorreoService correoService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public RecordatorioSeguimientoService(
            CitaRepository citaRepository,
            CorreoService correoService) {

        this.citaRepository = citaRepository;
        this.correoService = correoService;
    }

    // ==========================================
    // CU-12 - REVISIÓN AUTOMÁTICA
    // CADA 15 MINUTOS
    // ==========================================

    @Scheduled(fixedDelay = 900000)
    public void procesarRecordatorios() {

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(24);

        List<Cita> citas;

        try {

            citas = citaRepository
                    .buscarSeguimientosPendientesRecordatorio(
                            ahora.toLocalDate(),
                            limite.toLocalDate()
                    );

        } catch (Exception e) {

            log.error(
                    "CU-12: Error al consultar citas pendientes de recordatorio.",
                    e
            );

            return;
        }

        // ======================================
        // PROCESAR CADA CITA
        // ======================================

        for (Cita cita : citas) {

            try {

                // ==================================
                // VALIDAR ORIGEN Y ESTADO
                // ==================================

                if (!"SEGUIMIENTO".equals(
                        cita.getOrigenCita())) {

                    continue;
                }

                if (!"CONFIRMADA".equals(
                        cita.getEstado())) {

                    continue;
                }

                // ==================================
                // VALIDAR RECORDATORIO
                // ==================================

                if (Boolean.TRUE.equals(
                        cita.getRecordatorioSeguimientoEnviado())) {

                    continue;
                }

                // ==================================
                // OBTENER FECHA Y HORA
                // ==================================

                if (cita.getHorario() == null
                        || cita.getHorario().getFecha() == null
                        || cita.getHorario().getHora() == null) {

                    log.warn(
                            "CU-12: La cita {} no tiene horario válido.",
                            cita.getId()
                    );

                    continue;
                }

                LocalDateTime fechaCita =
                        LocalDateTime.of(
                                cita.getHorario().getFecha(),
                                cita.getHorario().getHora()
                        );

                // ==================================
                // VALIDAR VENTANA DE 24 HORAS
                // ==================================

                if (!fechaCita.isAfter(ahora)
                        || fechaCita.isAfter(limite)) {

                    continue;
                }

                // ==================================
                // ENVIAR RECORDATORIO
                // ==================================

                correoService.enviarRecordatorioSeguimiento(
                        cita
                );

                // ==================================
                // REGISTRAR ENVÍO
                // ==================================

                cita.setRecordatorioSeguimientoEnviado(true);

                citaRepository.save(cita);

                log.info(
                        "CU-12: Recordatorio enviado correctamente para cita {}.",
                        cita.getId()
                );

            } catch (Exception e) {

                log.error(
                        "CU-12: Error al procesar recordatorio de cita {}.",
                        cita.getId(),
                        e
                );
            }
        }
    }
}