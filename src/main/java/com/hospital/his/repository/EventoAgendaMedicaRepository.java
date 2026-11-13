package com.hospital.his.repository;

import com.hospital.his.entity.EventoAgendaMedica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoAgendaMedicaRepository
        extends JpaRepository<EventoAgendaMedica, Long> {

    // ==========================================
    // CU-16: EVENTOS DE UN MÉDICO
    // ==========================================

    List<EventoAgendaMedica> findByMedicoIdOrderByFechaInicioAsc(
            Long medicoId
    );


    // ==========================================
    // CU-16: EVENTOS EN UN RANGO DE FECHAS
    // ==========================================

    List<EventoAgendaMedica>
    findByMedicoIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
            Long medicoId,
            LocalDateTime fechaFinRango,
            LocalDateTime fechaInicioRango
    );


    // ==========================================
    // CU-16: BUSCAR EVENTO DE UN MÉDICO POR ID
    // ==========================================

    Optional<EventoAgendaMedica> findByIdAndMedicoId(
            Long eventoId,
            Long medicoId
    );

}