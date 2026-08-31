package com.hospital.his.repository;

import com.hospital.his.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByDpi(String dpi);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Page<Usuario> findByNombreCompletoContainingIgnoreCase(
            String nombreCompleto,
            Pageable pageable
    );

    Page<Usuario> findByCorreoElectronicoContainingIgnoreCase(
            String correoElectronico,
            Pageable pageable
    );

    Page<Usuario> findByRolContainingIgnoreCase(
            String rol,
            Pageable pageable
    );

    Page<Usuario> findByNombreUsuarioContainingIgnoreCase(
            String nombreUsuario,
            Pageable pageable
    );

    Page<Usuario> findByDpiContaining(
            String dpi,
            Pageable pageable
    );
    boolean existsByDpi(String dpi);

    boolean existsByCorreoElectronicoIgnoreCase(
            String correoElectronico
    );

    boolean existsByNombreUsuarioIgnoreCase(
            String nombreUsuario
    );
}