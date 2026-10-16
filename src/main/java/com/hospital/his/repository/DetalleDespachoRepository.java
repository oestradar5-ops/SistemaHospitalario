package com.hospital.his.repository;

import com.hospital.his.entity.DetalleDespacho;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleDespachoRepository
        extends JpaRepository<DetalleDespacho, Long> {

    // ==========================================
    // BUSCAR DETALLES POR DESPACHO
    // ==========================================

    List<DetalleDespacho> findByDespachoId(
            Long despachoId
    );

    // ==========================================
    // CONSULTAR CANTIDAD YA DESPACHADA
    // ==========================================

    @Query("""
            SELECT COALESCE(SUM(d.cantidad), 0)
            FROM DetalleDespacho d
            WHERE d.recetaMedicamento.id = :recetaMedicamentoId
              AND d.despacho.estado = 'CONFIRMADO'
            """)
    Long obtenerCantidadDespachada(
            @Param("recetaMedicamentoId")
            Long recetaMedicamentoId
    );
}