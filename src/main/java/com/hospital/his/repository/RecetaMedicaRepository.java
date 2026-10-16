package com.hospital.his.repository;

import com.hospital.his.entity.RecetaMedica;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecetaMedicaRepository
        extends JpaRepository<RecetaMedica, Long> {

    // ==========================================
    // BUSCAR RECETAS POR CITA
    // ==========================================

    List<RecetaMedica> findByCitaIdOrderByFechaCreacionDesc(
            Long citaId
    );


    // ==========================================
    // BUSCAR RECETA CON SUS MEDICAMENTOS
    // ==========================================

    @Query("""
            SELECT DISTINCT r
            FROM RecetaMedica r
            LEFT JOIN FETCH r.medicamentos rm
            LEFT JOIN FETCH rm.medicamento
            WHERE r.id = :recetaId
            """)
    Optional<RecetaMedica> buscarRecetaConMedicamentos(
            @Param("recetaId") Long recetaId
    );


    // ==========================================
    // BUSCAR RECETAS POR CITA CON MEDICAMENTOS
    // ==========================================

    @Query("""
            SELECT DISTINCT r
            FROM RecetaMedica r
            LEFT JOIN FETCH r.medicamentos rm
            LEFT JOIN FETCH rm.medicamento
            WHERE r.cita.id = :citaId
            ORDER BY r.fechaCreacion DESC
            """)
    List<RecetaMedica> buscarPorCitaConMedicamentos(
            @Param("citaId") Long citaId
    );


    // ==========================================
    // CU-11: BLOQUEAR RECETA PARA DESPACHO
    // ==========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM RecetaMedica r
            WHERE r.id = :recetaId
            """)
    Optional<RecetaMedica> bloquearRecetaParaDespacho(
            @Param("recetaId") Long recetaId
    );

}