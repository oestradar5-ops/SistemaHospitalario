package com.hospital.his.repository;

import com.hospital.his.entity.MovimientoInventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    // ==========================================
    // CU-15: LISTAR TODOS LOS MOVIMIENTOS
    // ==========================================

    @Query("""
            SELECT m
            FROM MovimientoInventario m
            JOIN FETCH m.inventario i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            JOIN FETCH m.usuario
            ORDER BY m.fechaHora DESC, m.id DESC
            """)
    List<MovimientoInventario> findAllConDetalles();


    // ==========================================
    // CU-15: MOVIMIENTOS POR INVENTARIO
    // ==========================================

    @Query("""
            SELECT m
            FROM MovimientoInventario m
            JOIN FETCH m.inventario i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            JOIN FETCH m.usuario
            WHERE i.id = :inventarioId
            ORDER BY m.fechaHora DESC, m.id DESC
            """)
    List<MovimientoInventario> findByInventarioConDetalles(
            @Param("inventarioId") Long inventarioId
    );


    // ==========================================
    // CU-15: MOVIMIENTOS POR TIPO
    // ==========================================

    @Query("""
            SELECT m
            FROM MovimientoInventario m
            JOIN FETCH m.inventario i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            JOIN FETCH m.usuario
            WHERE m.tipoMovimiento = :tipoMovimiento
            ORDER BY m.fechaHora DESC, m.id DESC
            """)
    List<MovimientoInventario> findByTipoConDetalles(
            @Param("tipoMovimiento") Integer tipoMovimiento
    );


    // ==========================================
    // CU-15: MOVIMIENTOS POR FECHAS
    // ==========================================

    @Query("""
            SELECT m
            FROM MovimientoInventario m
            JOIN FETCH m.inventario i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            JOIN FETCH m.usuario
            WHERE m.fechaHora BETWEEN :inicio AND :fin
            ORDER BY m.fechaHora DESC, m.id DESC
            """)
    List<MovimientoInventario> findByFechasConDetalles(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );


    // ==========================================
    // CU-15: MOVIMIENTOS POR SUCURSAL
    // ==========================================

    @Query("""
            SELECT m
            FROM MovimientoInventario m
            JOIN FETCH m.inventario i
            JOIN FETCH i.medicamento
            JOIN FETCH i.sucursal
            JOIN FETCH m.usuario
            WHERE i.sucursal.id = :sucursalId
            ORDER BY m.fechaHora DESC, m.id DESC
            """)
    List<MovimientoInventario> findBySucursalConDetalles(
            @Param("sucursalId") Long sucursalId
    );


    // ==========================================
    // CU-15: MOVIMIENTOS ACTIVOS
    // ==========================================

    List<MovimientoInventario> findByActivoTrueOrderByFechaHoraDesc();


    // ==========================================
    // CU-15: MOVIMIENTOS INACTIVOS
    // ==========================================

    List<MovimientoInventario> findByActivoFalseOrderByFechaHoraDesc();

}