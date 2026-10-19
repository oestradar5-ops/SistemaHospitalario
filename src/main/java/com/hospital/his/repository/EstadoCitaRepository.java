package com.hospital.his.repository;

import com.hospital.his.entity.EstadoCita;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoCitaRepository
        extends JpaRepository<EstadoCita, Long> {

    // ==========================================
    // CU-14
    // LISTAR TODOS LOS ESTADOS
    // ==========================================

    List<EstadoCita> findAllByOrderByNombreAsc();


    // ==========================================
    // CU-14
    // LISTAR ESTADOS ACTIVOS
    // ==========================================

    List<EstadoCita> findByActivoTrueOrderByNombreAsc();


    // ==========================================
    // CU-14
    // BUSCAR ESTADO POR CÓDIGO
    // ==========================================

    Optional<EstadoCita> findByCodigoIgnoreCase(
            String codigo
    );


    // ==========================================
    // CU-14
    // VALIDAR CÓDIGO DUPLICADO
    // ==========================================

    boolean existsByCodigoIgnoreCase(
            String codigo
    );


    // ==========================================
    // CU-14
    // VALIDAR DUPLICADO AL EDITAR
    // ==========================================

    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id
    );


    // ==========================================
    // CU-14
    // LISTAR ESTADOS PROTEGIDOS
    // ==========================================

    List<EstadoCita> findByProtegidoTrueOrderByNombreAsc();


    // ==========================================
    // CU-14
    // LISTAR ESTADOS NO PROTEGIDOS
    // ==========================================

    List<EstadoCita> findByProtegidoFalseOrderByNombreAsc();

}