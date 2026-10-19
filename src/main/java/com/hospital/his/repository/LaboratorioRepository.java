package com.hospital.his.repository;

import com.hospital.his.entity.Laboratorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaboratorioRepository
        extends JpaRepository<Laboratorio, Long> {

    // ==========================================
    // CU-14: LISTAR TODOS LOS LABORATORIOS
    // ==========================================

    List<Laboratorio> findAllByOrderByNombreAsc();


    // ==========================================
    // CU-14: LISTAR LABORATORIOS ACTIVOS
    // ==========================================

    List<Laboratorio> findByActivoTrueOrderByNombreAsc();


    // ==========================================
    // CU-14: BUSCAR POR NOMBRE
    // ==========================================

    Optional<Laboratorio> findByNombreIgnoreCase(
            String nombre
    );


    // ==========================================
    // CU-14: VALIDAR NOMBRE DUPLICADO
    // ==========================================

    boolean existsByNombreIgnoreCase(
            String nombre
    );


    // ==========================================
    // CU-14: VALIDAR DUPLICADOS AL EDITAR
    // ==========================================

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );


    // ==========================================
    // CU-14: LISTAR LABORATORIOS INACTIVOS
    // ==========================================

    List<Laboratorio> findByActivoFalseOrderByNombreAsc();

}