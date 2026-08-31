package com.hospital.his.repository;

import com.hospital.his.entity.SucursalEspecialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalEspecialidadRepository
        extends JpaRepository<SucursalEspecialidad, Long> {

    List<SucursalEspecialidad>
    findBySucursalIdAndEspecialidadActivoTrueOrderByEspecialidadNombreAsc(
            Long sucursalId
    );
}