package com.hospital.his.repository;

import com.hospital.his.entity.Cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // ==========================================
    // CITAS DEL PACIENTE
    // ==========================================

    List<Cita> findByPacienteIdOrderByFechaCreacionDesc(
            Long pacienteId
    );


    // ==========================================
    // CITAS VENCIDAS DEL PORTAL
    // ==========================================

    List<Cita> findByEstadoAndOrigenCitaAndFechaCreacionBefore(
            String estado,
            String origenCita,
            LocalDateTime fechaLimite
    );


    // ==========================================
    // CU-07 - LISTADO DE ENFERMERÍA
    // ==========================================

    List<Cita> findByEstadoOrderByFechaCreacionAsc(
            String estado
    );


    // ==========================================
    // CU-12 - VERIFICAR CITAS POR HORARIO
    // ==========================================

    boolean existsByHorarioId(
            Long horarioId
    );


    // ==========================================
    // CU-12 - CONSULTAR CITAS DE UN HORARIO
    // ==========================================

    List<Cita> findByHorarioId(
            Long horarioId
    );


    // ==========================================
    // CU-12 - CITAS PENDIENTES DE RECORDATORIO
    // ==========================================

    @Query("""
            SELECT c
            FROM Cita c
            JOIN c.horario h
            WHERE c.origenCita = 'SEGUIMIENTO'
              AND c.estado = 'CONFIRMADA'
              AND (
                  c.recordatorioSeguimientoEnviado = false
                  OR c.recordatorioSeguimientoEnviado IS NULL
              )
              AND h.fecha >= :fechaActual
              AND h.fecha <= :fechaLimite
            ORDER BY h.fecha ASC, h.hora ASC
            """)
    List<Cita> buscarSeguimientosPendientesRecordatorio(
            @Param("fechaActual") LocalDate fechaActual,
            @Param("fechaLimite") LocalDate fechaLimite
    );

}