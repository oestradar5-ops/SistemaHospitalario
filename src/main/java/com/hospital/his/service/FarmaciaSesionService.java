package com.hospital.his.service;

import com.hospital.his.entity.Sucursal;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.SucursalRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

@Service
public class FarmaciaSesionService {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final UsuarioRepository usuarioRepository;

    private final SucursalRepository sucursalRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaSesionService(
            UsuarioRepository usuarioRepository,
            SucursalRepository sucursalRepository) {

        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
    }

    // ==========================================
    // OBTENER EMPLEADO AUTENTICADO
    // ==========================================

    public Usuario obtenerUsuario(HttpSession sesion) {

        if (sesion == null) {
            throw new SecurityException(
                    "Debe iniciar sesión en Farmacia."
            );
        }

        Object nombreUsuario =
                sesion.getAttribute("usuario");

        Object rol =
                sesion.getAttribute("rol");

        if (!(nombreUsuario instanceof String)
                || !(rol instanceof String)) {

            throw new SecurityException(
                    "Sesión de Farmacia no válida."
            );
        }

        if (!"FARMACIA".equalsIgnoreCase(
                (String) rol)) {

            throw new SecurityException(
                    "No tiene permisos para acceder a Farmacia."
            );
        }

        Usuario usuario = usuarioRepository
                .findByNombreUsuario((String) nombreUsuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "El usuario no existe."
                        )
                );

        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            throw new SecurityException(
                    "La cuenta del usuario está inactiva."
            );
        }

        if (!"FARMACIA".equalsIgnoreCase(
                usuario.getRol())) {

            throw new SecurityException(
                    "El usuario no pertenece a Farmacia."
            );
        }

        return usuario;
    }

    // ==========================================
    // OBTENER SUCURSAL DEL EMPLEADO
    // ==========================================

    public Sucursal obtenerSucursal(HttpSession sesion) {

        Usuario usuario = obtenerUsuario(sesion);

        String nombreSucursal = usuario.getSucursal();

        if (nombreSucursal == null
                || nombreSucursal.isBlank()) {

            throw new IllegalStateException(
                    "El empleado no tiene una sucursal asignada."
            );
        }

        return sucursalRepository
                .findByNombreIgnoreCaseAndActivoTrue(
                        nombreSucursal.trim()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "La sucursal asignada no existe "
                                        + "o está inactiva."
                        )
                );
    }
}