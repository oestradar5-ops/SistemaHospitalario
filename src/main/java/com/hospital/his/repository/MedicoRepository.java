package com.hospital.his.repository;

import com.hospital.his.entity.Medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository
        extends JpaRepository<Medico, Long> {

    // ==========================================
    // MÉTODO ORIGINAL
    // BUSCAR MÉDICOS ACTIVOS POR SUCURSAL
    // Y ESPECIALIDAD
    // ==========================================

    List<Medico>
    findBySucursalIdAndEspecialidadIdAndActivoTrue(
            Long sucursalId,
            Long especialidadId
    );


    // ==========================================
    // CU-16: BUSCAR MÉDICO POR USUARIO
    // ==========================================

    Optional<Medico> findByUsuarioId(
            Long usuarioId
    );


    // ==========================================
    // CU-16: BUSCAR MÉDICO ACTIVO
    // POR NOMBRE DE USUARIO
    // ==========================================

    Optional<Medico> findByUsuarioNombreUsuarioAndActivoTrue(
            String nombreUsuario
    );

}