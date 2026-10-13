package com.hospital.his.repository;

import com.hospital.his.entity.RecetaMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaMedicamentoRepository
        extends JpaRepository<RecetaMedicamento, Long> {

    // Obtener los medicamentos de una receta

    List<RecetaMedicamento> findByRecetaId(
            Long recetaId
    );

}