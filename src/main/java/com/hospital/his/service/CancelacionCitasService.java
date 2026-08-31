package com.hospital.his.service;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.HorarioMedico;
import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.HorarioMedicoRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CancelacionCitasService {

    private final CitaRepository citaRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;


    public CancelacionCitasService(
            CitaRepository citaRepository,
            HorarioMedicoRepository horarioMedicoRepository) {

        this.citaRepository = citaRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
    }


    // =====================================================
    // CANCELAR CITAS VENCIDAS DEL PORTAL
    // =====================================================

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelarCitasPendientesVencidas() {

        LocalDateTime fechaLimite =
                LocalDateTime.now()
                        .minusMinutes(10);


        List<Cita> citasVencidas =
                citaRepository
                        .findByEstadoAndOrigenCitaAndFechaCreacionBefore(
                                "PENDIENTE_DE_PAGO",
                                "PORTAL",
                                fechaLimite
                        );


        if (citasVencidas.isEmpty()) {

            return;
        }


        for (Cita cita : citasVencidas) {

            // =============================================
            // CANCELAR CITA
            // =============================================

            cita.setEstado(
                    "CANCELADA"
            );


            citaRepository.save(
                    cita
            );


            // =============================================
            // LIBERAR HORARIO
            // =============================================

            HorarioMedico horario =
                    cita.getHorario();


            if (horario != null) {

                horario.setDisponible(
                        true
                );


                horarioMedicoRepository.save(
                        horario
                );
            }


            System.out.println(
                    "CITA CANCELADA AUTOMÁTICAMENTE | " +
                            "Cita: " +
                            cita.getId() +
                            " | Paciente: " +
                            cita.getPaciente()
                                    .getNombreCompleto() +
                            " | Creada: " +
                            cita.getFechaCreacion()
            );
        }
    }
}