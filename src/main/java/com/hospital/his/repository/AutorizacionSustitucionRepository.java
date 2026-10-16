package com.hospital.his.repository;

import com.hospital.his.entity.AutorizacionSustitucion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface AutorizacionSustitucionRepository
        extends JpaRepository<AutorizacionSustitucion, Long> {

    // Solicitudes de una sucursal
    List<AutorizacionSustitucion>
    findBySucursalIdOrderByFechaSolicitudDesc(Long sucursalId);

    // Solicitudes pendientes
    List<AutorizacionSustitucion>
    findByEstadoOrderByFechaSolicitudAsc(String estado);

    // Autorizaciones relacionadas con un medicamento recetado
    List<AutorizacionSustitucion>
    findByRecetaMedicamentoIdOrderByFechaSolicitudDesc(
            Long recetaMedicamentoId
    );

    // Bloqueo para evitar dos respuestas simultáneas
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM AutorizacionSustitucion a
            WHERE a.id = :id
            """)
    Optional<AutorizacionSustitucion> bloquearPorId(
            @Param("id") Long id
    );
}