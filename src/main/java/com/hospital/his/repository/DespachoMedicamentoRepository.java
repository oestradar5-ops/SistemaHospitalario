package com.hospital.his.repository;

import com.hospital.his.entity.DespachoMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DespachoMedicamentoRepository
        extends JpaRepository<DespachoMedicamento, Long> {

    List<DespachoMedicamento>
    findByRecetaIdOrderByFechaDespachoDesc(
            Long recetaId
    );

    List<DespachoMedicamento>
    findBySucursalIdOrderByFechaDespachoDesc(
            Long sucursalId
    );

    boolean existsByRecetaIdAndEstado(
            Long recetaId,
            String estado
    );
}