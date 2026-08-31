package com.hospital.his.repository;

import com.hospital.his.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {
}