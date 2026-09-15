package com.hospital.his.repository;

import com.hospital.his.entity.ConsultaMedica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultaMedicaRepository
        extends JpaRepository<ConsultaMedica, Long> {

    Optional<ConsultaMedica>
    findByCitaId(Long citaId);
}