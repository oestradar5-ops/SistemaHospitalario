package com.hospital.his.repository;

import com.hospital.his.entity.HorarioMedico;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HorarioMedicoRepository
        extends JpaRepository<HorarioMedico, Long> {

    // ==========================================
    // HORARIOS DISPONIBLES DE UN MÉDICO
    // ==========================================

    List<HorarioMedico>
    findByMedicoIdAndDisponibleTrueOrderByFechaAscHoraAsc(
            Long medicoId
    );

    // ==========================================
    // HORARIOS DISPONIBLES POR FECHA
    // ==========================================

    List<HorarioMedico>
    findByMedicoIdAndFechaAndDisponibleTrueOrderByHoraAsc(
            Long medicoId,
            LocalDate fecha
    );

    // ==========================================
    // CU-12 - BLOQUEAR HORARIO PARA RESERVA
    // ==========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT h
            FROM HorarioMedico h
            WHERE h.id = :horarioId
            """)
    Optional<HorarioMedico> buscarHorarioParaReserva(
            @Param("horarioId") Long horarioId
    );
}