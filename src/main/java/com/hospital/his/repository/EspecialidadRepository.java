package com.hospital.his.repository;

import com.hospital.his.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {

    // ==========================================
    // LISTAR ESPECIALIDADES ACTIVAS
    // ==========================================

    List<Especialidad> findByActivoTrueOrderByNombreAsc();

}