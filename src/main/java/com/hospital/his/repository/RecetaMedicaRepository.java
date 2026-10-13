package com.hospital.his.repository;

import com.hospital.his.entity.RecetaMedica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaMedicaRepository
        extends JpaRepository<RecetaMedica, Long> {

    // Buscar todas las recetas de una cita
    // Ordenadas desde la más reciente

    List<RecetaMedica> findByCitaIdOrderByFechaCreacionDesc(
            Long citaId
    );

}