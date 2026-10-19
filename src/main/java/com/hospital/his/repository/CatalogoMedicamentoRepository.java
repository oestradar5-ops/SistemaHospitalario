package com.hospital.his.repository;

import com.hospital.his.entity.CatalogoMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoMedicamentoRepository
        extends JpaRepository<CatalogoMedicamento, Long> {

    // ==========================================
    // MÉTODO ORIGINAL
    // CONSERVADO PARA CASOS ANTERIORES
    // ==========================================

    // Obtener únicamente medicamentos activos
    // Ordenados alfabéticamente

    List<CatalogoMedicamento> findByActivoTrueOrderByNombreAsc();


    // ==========================================
    // CU-14: LISTAR TODOS LOS MEDICAMENTOS
    // ==========================================

    List<CatalogoMedicamento> findAllByOrderByNombreAsc();


    // ==========================================
    // CU-14: BUSCAR MEDICAMENTO POR NOMBRE
    // ==========================================

    Optional<CatalogoMedicamento> findByNombreIgnoreCase(
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
    // CU-14: LISTAR MEDICAMENTOS INACTIVOS
    // ==========================================

    List<CatalogoMedicamento> findByActivoFalseOrderByNombreAsc();


    // ==========================================
    // CU-14: BUSCAR MEDICAMENTOS POR NOMBRE
    // ==========================================

    List<CatalogoMedicamento> findByNombreContainingIgnoreCaseOrderByNombreAsc(
            String nombre
    );


    // ==========================================
    // CU-14: BUSCAR MEDICAMENTOS POR PRESENTACIÓN
    // ==========================================

    List<CatalogoMedicamento> findByPresentacionContainingIgnoreCaseOrderByNombreAsc(
            String presentacion
    );

}