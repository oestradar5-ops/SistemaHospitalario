package com.hospital.his.repository;

import com.hospital.his.entity.InventarioMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface InventarioMedicamentoRepository
        extends JpaRepository<InventarioMedicamento, Long> {

    // ==========================================
    // BUSCAR INVENTARIO POR SUCURSAL
    // ==========================================

    List<InventarioMedicamento> findBySucursalIdAndActivoTrue(
            Long sucursalId
    );

    // ==========================================
    // BUSCAR MEDICAMENTO EN UNA SUCURSAL
    // ==========================================

    Optional<InventarioMedicamento>
    findByMedicamentoIdAndSucursalId(
            Long medicamentoId,
            Long sucursalId
    );

    // ==========================================
    // BUSCAR INVENTARIO ACTIVO
    // ==========================================

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
}