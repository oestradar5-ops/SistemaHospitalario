package com.hospital.his.repository;

import com.hospital.his.entity.Especialidad;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {

    // ==========================================
    // CU-03 Y CU-13
    // LISTAR ESPECIALIDADES ACTIVAS
    // ==========================================

    List<Especialidad> findByActivoTrueOrderByNombreAsc();


    // ==========================================
    // CU-14
    // LISTAR TODAS LAS ESPECIALIDADES
    // ORDENADAS POR NOMBRE
    // ==========================================

    List<Especialidad> findAllByOrderByNombreAsc();


    // ==========================================
    // CU-14
    // BUSCAR ESPECIALIDAD POR NOMBRE
    // ==========================================

    Optional<Especialidad> findByNombreIgnoreCase(
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

}