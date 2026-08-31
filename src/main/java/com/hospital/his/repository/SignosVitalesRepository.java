package com.hospital.his.repository;

import com.hospital.his.entity.SignosVitales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignosVitalesRepository
        extends JpaRepository<SignosVitales, Long> {

    Optional<SignosVitales> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);
}