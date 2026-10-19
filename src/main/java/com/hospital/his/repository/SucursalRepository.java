package com.hospital.his.repository;

import com.hospital.his.entity.Sucursal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository
        extends JpaRepository<Sucursal, Long> {

    // ==========================================
    // CU-03 Y CU-13
    // LISTAR SUCURSALES ACTIVAS
    // ==========================================

    List<Sucursal> findByActivoTrueOrderByNombreAsc();


    // ==========================================
    // CU-03 Y CU-13
    // BUSCAR SUCURSAL ACTIVA POR NOMBRE
    // ==========================================

    Optional<Sucursal> findByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );


    // ==========================================
    // CU-14
    // LISTAR TODAS LAS SUCURSALES
    // ACTIVAS E INACTIVAS
    // ==========================================

    List<Sucursal> findAllByOrderByNombreAsc();


    // ==========================================
    // CU-14
    // BUSCAR SUCURSAL POR NOMBRE
    // ==========================================

    Optional<Sucursal> findByNombreIgnoreCase(
            String nombre
    );


    // ==========================================
    // CU-14
    // VALIDAR NOMBRE DUPLICADO AL CREAR
    // ==========================================

    boolean existsByNombreIgnoreCase(
            String nombre
    );


    // ==========================================
    // CU-14
    // VALIDAR NOMBRE DUPLICADO AL EDITAR
    // EXCLUYENDO EL REGISTRO ACTUAL
    // ==========================================

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );


    // ==========================================
    // CU-14
    // VALIDAR DUPLICADOS ACTIVOS
    // ==========================================

    boolean existsByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );


    // ==========================================
    // CU-14
    // VALIDAR DUPLICADOS ACTIVOS AL EDITAR
    // ==========================================

    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
            String nombre,
            Long id
    );

}