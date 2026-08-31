package com.hospital.his.repository;

import com.hospital.his.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository
        extends JpaRepository<Medico, Long> {

    List<Medico>
    findBySucursalIdAndEspecialidadIdAndActivoTrue(
            Long sucursalId,
            Long especialidadId
    );
}