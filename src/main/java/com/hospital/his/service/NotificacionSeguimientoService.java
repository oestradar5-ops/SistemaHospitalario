package com.hospital.his.service;

import com.hospital.his.entity.Cita;
import com.hospital.his.event.CitaSeguimientoRegistradaEvent;
import com.hospital.his.repository.CitaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificacionSeguimientoService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NotificacionSeguimientoService.class
            );

    private final CitaRepository citaRepository;
    private final CorreoService correoService;

    public NotificacionSeguimientoService(
            CitaRepository citaRepository,
            CorreoService correoService) {

        this.citaRepository = citaRepository;
        this.correoService = correoService;
    }

    // ==========================================
    // CU-12 - CONFIRMACIÓN DE SEGUIMIENTO
    // ==========================================

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void enviarConfirmacion(
            CitaSeguimientoRegistradaEvent evento) {

        try {

            enviarCorreoConfirmacion(evento.citaId());

        } catch (Exception e) {

            log.error(
                    "CU-12: No fue posible enviar la confirmación de la cita {}.",
                    evento.citaId(),
                    e
            );
        }
    }

    // ==========================================
    // CONSULTAR CITA Y ENVIAR CORREO
    // ==========================================

    private void enviarCorreoConfirmacion(Long citaId) {

        Cita cita = citaRepository
                .findById(citaId)
                .orElse(null);

        if (cita == null) {

            log.warn(
                    "CU-12: No se encontró la cita {}.",
                    citaId
            );

            return;
        }

        if (!"SEGUIMIENTO".equals(
                cita.getOrigenCita())) {

            return;
        }

        correoService.enviarConfirmacionSeguimiento(cita);

        log.info(
                "CU-12: Correo de confirmación enviado para la cita {}.",
                cita.getId()
        );
    }
}