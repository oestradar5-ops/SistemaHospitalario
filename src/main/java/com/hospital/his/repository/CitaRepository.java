package com.hospital.his.repository;

import com.hospital.his.entity.Cita;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface CitaRepository
        extends JpaRepository<Cita, Long> {


    // CITAS DEL PACIENTE
    List<Cita> findByPacienteIdOrderByFechaCreacionDesc(
            Long pacienteId
    );


    // CITAS VENCIDAS DEL PORTAL
    List<Cita> findByEstadoAndOrigenCitaAndFechaCreacionBefore(
            String estado,
            String origenCita,
            LocalDateTime fechaLimite
    );


    // CU-07 - LISTADO DE ENFERMERÍA
    List<Cita> findByEstadoOrderByFechaCreacionAsc(
            String estado
    );
}