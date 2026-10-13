package com.hospital.his.repository;

import com.hospital.his.entity.SeguimientoMedico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoMedicoRepository
        extends JpaRepository<SeguimientoMedico, Long> {

    // ==========================================
    // BUSCAR SEGUIMIENTOS DE UNA CITA
    // ==========================================

    List<SeguimientoMedico>
    findByCitaIdOrderByFechaCreacionDesc(Long citaId);

}