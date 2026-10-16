package com.hospital.his.repository;

import com.hospital.his.entity.Pago;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository
        extends JpaRepository<Pago, Long> {

    // ==========================================
    // PAGOS DE CONSULTAS MÉDICAS
    // ==========================================

    Optional<Pago> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);


    // ==========================================
    // CU-09: PAGOS DE LABORATORIO
    // ==========================================

    // Buscar pago asociado a una orden de laboratorio
    Optional<Pago> findByOrdenLaboratorioId(Long ordenId);

    // Verificar si existe un pago para la orden
    boolean existsByOrdenLaboratorioId(Long ordenId);

    // Verificar si existe un pago aprobado
    boolean existsByOrdenLaboratorioIdAndEstado(
            Long ordenId,
            String estado
    );
}