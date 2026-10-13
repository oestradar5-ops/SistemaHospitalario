package com.hospital.his.repository;

import com.hospital.his.entity.CatalogoExamen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogoExamenRepository
        extends JpaRepository<CatalogoExamen, Long> {

    List<CatalogoExamen> findByActivoTrueOrderByNombreAsc();

}