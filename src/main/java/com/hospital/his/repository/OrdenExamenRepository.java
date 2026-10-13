package com.hospital.his.repository;

import com.hospital.his.entity.OrdenExamen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenExamenRepository
        extends JpaRepository<OrdenExamen, Long> {

    List<OrdenExamen> findByOrdenId(Long ordenId);

}