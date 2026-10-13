package com.hospital.his.repository;

import com.hospital.his.entity.OrdenLaboratorio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenLaboratorioRepository
        extends JpaRepository<OrdenLaboratorio, Long> {

    List<OrdenLaboratorio> findByCitaIdOrderByFechaCreacionDesc(
            Long citaId
    );

}