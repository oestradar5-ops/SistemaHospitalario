package com.hospital.his.repository;

import com.hospital.his.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository
        extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivoTrueOrderByNombreAsc();
}