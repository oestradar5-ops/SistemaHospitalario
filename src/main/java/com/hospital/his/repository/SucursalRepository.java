package com.hospital.his.repository;

import com.hospital.his.entity.Sucursal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository
        extends JpaRepository<Sucursal, Long> {

    // ==========================================
    // LISTAR SUCURSALES ACTIVAS
    // ==========================================

    List<Sucursal> findByActivoTrueOrderByNombreAsc();

    // ==========================================
    // BUSCAR SUCURSAL ACTIVA POR NOMBRE
    // ==========================================

    Optional<Sucursal> findByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );

}