package com.hospital.his.repository;

import com.hospital.his.entity.RechazoMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechazoMedicamentoRepository
        extends JpaRepository<RechazoMedicamento, Long> {

    // ==========================================
    // BUSCAR RECHAZOS POR RECETA
    // ==========================================

    List<RechazoMedicamento>
    findByRecetaIdOrderByFechaRechazoDesc(
            Long recetaId
    );

    // ==========================================
    // BUSCAR RECHAZOS POR SUCURSAL
    // ==========================================

    List<RechazoMedicamento>
    findBySucursalIdOrderByFechaRechazoDesc(
            Long sucursalId
    );

    // ==========================================
    // COMPROBAR SI EXISTE UN RECHAZO
    // ==========================================

    boolean existsByRecetaIdAndEstado(
            Long recetaId,
            String estado
    );

}