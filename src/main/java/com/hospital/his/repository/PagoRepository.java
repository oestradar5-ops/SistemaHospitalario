package com.hospital.his.repository;

import com.hospital.his.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository
        extends JpaRepository<Pago, Long> {

    Optional<Pago> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);
}