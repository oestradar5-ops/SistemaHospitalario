package com.hospital.his.repository;

import com.hospital.his.entity.InventarioMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioMedicamentoRepository
        extends JpaRepository<InventarioMedicamento, Long> {

    // ==========================================
    // MÉTODOS ORIGINALES
    // CONSERVADOS PARA LOS CASOS ANTERIORES
    // ==========================================

    // BUSCAR INVENTARIO POR SUCURSAL

    List<InventarioMedicamento> findBySucursalIdAndActivoTrue(
            Long sucursalId
    );


    // BUSCAR MEDICAMENTO EN UNA SUCURSAL

    Optional<InventarioMedicamento>
    findByMedicamentoIdAndSucursalId(
            Long medicamentoId,
            Long sucursalId
    );


    // BUSCAR INVENTARIO ACTIVO

    Optional<InventarioMedicamento>
    findByMedicamentoIdAndSucursalIdAndActivoTrue(
            Long medicamentoId,
            Long sucursalId
    );


    // ==========================================
    // BLOQUEAR INVENTARIO DURANTE UN DESPACHO
    // EVITA DESCUENTOS SIMULTÁNEOS INCORRECTOS
    // ==========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            WHERE i.medicamento.id = :medicamentoId
              AND i.sucursal.id = :sucursalId
              AND i.activo = true
            """)
    Optional<InventarioMedicamento>
    buscarParaDespacho(
            @Param("medicamentoId") Long medicamentoId,
            @Param("sucursalId") Long sucursalId
    );


    // ==========================================
    // CONSULTAR INVENTARIO CON STOCK BAJO
    // MÉTODO ORIGINAL
    // ==========================================

    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            WHERE i.sucursal.id = :sucursalId
              AND i.activo = true
              AND i.cantidadDisponible <= i.stockMinimo
            """)
    List<InventarioMedicamento> buscarStockBajo(
            @Param("sucursalId") Long sucursalId
    );


    // ==========================================
    // CU-14: LISTAR TODO EL INVENTARIO
    // CON MEDICAMENTO Y SUCURSAL
    // ==========================================

    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            ORDER BY i.medicamento.nombre ASC
            """)
    List<InventarioMedicamento> findAllConDetalles();


    // ==========================================
    // CU-14: LISTAR INVENTARIO POR SUCURSAL
    // INCLUYE REGISTROS ACTIVOS E INACTIVOS
    // ==========================================

    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            WHERE i.sucursal.id = :sucursalId
            ORDER BY i.medicamento.nombre ASC
            """)
    List<InventarioMedicamento> findAllBySucursalConDetalles(
            @Param("sucursalId") Long sucursalId
    );


    // ==========================================
    // CU-14: LISTAR INVENTARIO INACTIVO
    // ==========================================

    List<InventarioMedicamento> findByActivoFalse();


    // ==========================================
    // CU-14: VALIDAR INVENTARIO DUPLICADO
    // ==========================================

    boolean existsByMedicamentoIdAndSucursalId(
            Long medicamentoId,
            Long sucursalId
    );


    // ==========================================
    // CU-14: VALIDAR DUPLICADOS AL EDITAR
    // ==========================================

    boolean existsByMedicamentoIdAndSucursalIdAndIdNot(
            Long medicamentoId,
            Long sucursalId,
            Long id
    );


    // ==========================================
    // CU-14: LISTAR INVENTARIO POR MEDICAMENTO
    // ==========================================

    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            WHERE i.medicamento.id = :medicamentoId
            ORDER BY i.sucursal.nombre ASC
            """)
    List<InventarioMedicamento> findByMedicamentoConDetalles(
            @Param("medicamentoId") Long medicamentoId
    );


    // ==========================================
    // CU-14: CONSULTAR STOCK BAJO GENERAL
    // ==========================================

    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            WHERE i.activo = true
              AND i.cantidadDisponible <= i.stockMinimo
            ORDER BY i.sucursal.nombre ASC,
                     i.medicamento.nombre ASC
            """)
    List<InventarioMedicamento> buscarStockBajoGeneral();


    // ==========================================
    // CU-14: BLOQUEAR REGISTRO PARA ACTUALIZAR
    // ==========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InventarioMedicamento i
            WHERE i.id = :id
            """)
    Optional<InventarioMedicamento> buscarParaActualizar(
            @Param("id") Long id
    );

}