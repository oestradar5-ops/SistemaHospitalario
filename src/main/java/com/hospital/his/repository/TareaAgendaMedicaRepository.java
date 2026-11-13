package com.hospital.his.repository;

import com.hospital.his.entity.TareaAgendaMedica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TareaAgendaMedicaRepository
        extends JpaRepository<TareaAgendaMedica, Long> {

    // ==========================================
    // CU-16: TODAS LAS TAREAS DE UN MÉDICO
    // ==========================================

    List<TareaAgendaMedica> findByMedicoIdOrderByFechaLimiteAsc(
            Long medicoId
    );


    // ==========================================
    // CU-16: TAREAS PENDIENTES O COMPLETADAS
    // ==========================================

    List<TareaAgendaMedica>
    findByMedicoIdAndIsCompletedOrderByFechaLimiteAsc(
            Long medicoId,
            Boolean isCompleted
    );


    // ==========================================
    // CU-16: TAREAS POR FECHA LÍMITE
    // ==========================================

    List<TareaAgendaMedica>
    findByMedicoIdAndFechaLimiteGreaterThanEqualAndFechaLimiteLessThanOrderByFechaLimiteAsc(
            Long medicoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );


    // ==========================================
    // CU-16: BUSCAR TAREA POR ID Y MÉDICO
    // ==========================================

    Optional<TareaAgendaMedica> findByIdAndMedicoId(
            Long tareaId,
            Long medicoId
    );

}