package com.hospital.his.repository;

import com.hospital.his.entity.SucursalEspecialidad;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SucursalEspecialidadRepository
        extends JpaRepository<SucursalEspecialidad, Long> {

    // ==========================================
    // CONSULTA ORIGINAL - CONSERVADA
    // ==========================================

    List<SucursalEspecialidad>
    findBySucursalIdAndEspecialidadActivoTrueOrderByEspecialidadNombreAsc(
            Long sucursalId
    );


    // ==========================================
    // LISTAR ASIGNACIONES
    // ==========================================

    @Query("""
            SELECT se
            FROM SucursalEspecialidad se
            JOIN FETCH se.sucursal
            JOIN FETCH se.especialidad
            ORDER BY se.id DESC
            """)
    List<SucursalEspecialidad> listarAsignaciones();


    // ==========================================
    // BUSCAR POR ID CON PAGINACIÓN
    // ==========================================

    @Query(
            value = """
                    SELECT se
                    FROM SucursalEspecialidad se
                    JOIN FETCH se.sucursal
                    JOIN FETCH se.especialidad
                    WHERE (:id IS NULL OR se.id = :id)
                    """,
            countQuery = """
                    SELECT COUNT(se)
                    FROM SucursalEspecialidad se
                    WHERE (:id IS NULL OR se.id = :id)
                    """
    )
    Page<SucursalEspecialidad> buscarAsignaciones(
            @Param("id") Long id,
            Pageable pageable
    );


    // ==========================================
    // VALIDAR ASIGNACIÓN ACTIVA
    // ==========================================

    boolean existsBySucursalIdAndEspecialidadIdAndActivoTrue(
            Long sucursalId,
            Long especialidadId
    );


    // ==========================================
    // BUSCAR ASIGNACIÓN EXISTENTE
    // ==========================================

    Optional<SucursalEspecialidad>
    findBySucursalIdAndEspecialidadId(
            Long sucursalId,
            Long especialidadId
    );


    // ==========================================
    // ESPECIALIDADES ACTIVAS DE UNA SEDE
    // ==========================================

    List<SucursalEspecialidad>
    findBySucursalIdAndActivoTrueAndEspecialidadActivoTrueOrderByEspecialidadNombreAsc(
            Long sucursalId
    );

}