package com.hospital.his.repository;

import com.hospital.his.entity.OrdenLaboratorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenLaboratorioRepository
        extends JpaRepository<OrdenLaboratorio, Long> {

    // ==========================================
    // CU-08: CONSULTAR ÓRDENES POR CITA
    // ==========================================

    List<OrdenLaboratorio> findByCitaIdOrderByFechaCreacionDesc(
            Long citaId
    );

    // ==========================================
    // CU-09: CONSULTAR TODAS LAS ÓRDENES
    // ==========================================

    List<OrdenLaboratorio> findAllByOrderByFechaCreacionDesc();

    // ==========================================
    // CU-10: BUSCAR ÓRDENES PENDIENTES POR DPI
    // ==========================================

    @Query("""
            SELECT o
            FROM OrdenLaboratorio o
            JOIN o.cita c
            JOIN c.paciente p
            WHERE p.dpi = :dpi
              AND o.estado = 'PENDIENTE'
              AND o.fechaPago IS NULL
            ORDER BY o.fechaCreacion DESC
            """)
    List<OrdenLaboratorio> buscarPendientesPorDpi(
            @Param("dpi") String dpi
    );

}