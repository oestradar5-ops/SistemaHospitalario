package com.hospital.his.repository;

import com.hospital.his.entity.CatalogoMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogoMedicamentoRepository
        extends JpaRepository<CatalogoMedicamento, Long> {

    // Obtener únicamente medicamentos activos
    // Ordenados alfabéticamente

    List<CatalogoMedicamento> findByActivoTrueOrderByNombreAsc();

}