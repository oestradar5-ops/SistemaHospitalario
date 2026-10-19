package com.hospital.his.repository;

import com.hospital.his.entity.CatalogoExamen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoExamenRepository
        extends JpaRepository<CatalogoExamen, Long> {

    // ==========================================
    // MÉTODO ORIGINAL
    // CONSERVADO PARA LOS CASOS ANTERIORES
    // ==========================================

    List<CatalogoExamen> findByActivoTrueOrderByNombreAsc();


    // ==========================================
    // CU-14: LISTAR TODOS LOS EXÁMENES
    // ==========================================

    List<CatalogoExamen> findAllByOrderByNombreAsc();


    // ==========================================
    // CU-14: BUSCAR EXAMEN POR NOMBRE
    // ==========================================

    Optional<CatalogoExamen> findByNombreIgnoreCase(
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
    // CU-14: LISTAR EXÁMENES INACTIVOS
    // ==========================================

    List<CatalogoExamen> findByActivoFalseOrderByNombreAsc();


    // ==========================================
    // CU-14: BUSCAR EXÁMENES POR LABORATORIO
    // ==========================================

    List<CatalogoExamen> findByLaboratorioIdOrderByNombreAsc(
            Long laboratorioId
    );


    // ==========================================
    // CU-14: EXÁMENES ACTIVOS POR LABORATORIO
    // ==========================================

    List<CatalogoExamen> findByLaboratorioIdAndActivoTrueOrderByNombreAsc(
            Long laboratorioId
    );


    // ==========================================
    // CU-14: COMPROBAR EXÁMENES ASOCIADOS
    // ==========================================

    boolean existsByLaboratorioId(
            Long laboratorioId
    );

    // ==========================================
// CU-14
// LISTAR EXÁMENES CON SU LABORATORIO
// ==========================================

    @Query("""
        SELECT e
        FROM CatalogoExamen e
        LEFT JOIN FETCH e.laboratorio
        ORDER BY e.nombre ASC
        """)
    List<CatalogoExamen> findAllConLaboratorio();
}