package com.hospital.his.repository;

import com.hospital.his.entity.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // ==========================================
    // LISTAR TODOS LOS ROLES
    // ==========================================

    List<Rol> findAllByOrderByCodigoAsc();

    // ==========================================
    // LISTAR ROLES ACTIVOS
    // ==========================================

    List<Rol> findByActivoTrueOrderByCodigoAsc();

    // ==========================================
    // LISTAR ROLES INACTIVOS
    // ==========================================

    List<Rol> findByActivoFalseOrderByCodigoAsc();

    // ==========================================
    // BUSCAR ROL POR CÓDIGO
    // ==========================================

    Optional<Rol> findByCodigoIgnoreCase(String codigo);

    // ==========================================
    // VALIDAR CÓDIGO DUPLICADO
    // ==========================================

    boolean existsByCodigoIgnoreCase(String codigo);

    // ==========================================
    // VALIDAR DUPLICADOS AL EDITAR
    // ==========================================

    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id
    );

    // ==========================================
    // BUSCAR ROLES POR NOMBRE
    // ==========================================

    List<Rol> findByNombreContainingIgnoreCaseOrderByCodigoAsc(
            String nombre
    );

    // ==========================================
    // BUSCAR ROLES PROTEGIDOS
    // ==========================================

    List<Rol> findByProtegidoTrueOrderByCodigoAsc();

    // ==========================================
    // BUSCAR ROLES NO PROTEGIDOS
    // ==========================================

    List<Rol> findByProtegidoFalseOrderByCodigoAsc();
}