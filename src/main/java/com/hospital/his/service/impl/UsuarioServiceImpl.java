package com.hospital.his.service.impl;

import com.hospital.his.dto.LoginResponse;
import com.hospital.his.dto.VerificacionDpiResponse;
import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.UsuarioRepository;
import com.hospital.his.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // VERIFICAR DPI
    // ==========================================

    @Override
    public VerificacionDpiResponse verificarDpi(String dpi) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByDpi(dpi);

        if (usuarioOptional.isEmpty()) {

            return new VerificacionDpiResponse(
                    false,
                    "NO_REGISTRADO",
                    "No se encontró un registro asociado a este DPI. Será redirigido al formulario de registro."
            );
        }

        Usuario usuario = usuarioOptional.get();

        if ("PACIENTE".equalsIgnoreCase(usuario.getRol())) {

            return new VerificacionDpiResponse(
                    true,
                    "PACIENTE",
                    "Paciente registrado."
            );
        }

        return new VerificacionDpiResponse(
                true,
                "INTERNO",
                "Este DPI pertenece a un usuario del sistema interno. Por favor, contacte a recepción."
        );
    }


    // ==========================================
    // LOGIN DEL PACIENTE
    // ==========================================

    @Override
    public LoginResponse iniciarSesion(
            String nombreUsuario,
            String password) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOptional.isEmpty()) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }

        Usuario usuario = usuarioOptional.get();

        if (!usuario.getPassword().equals(password)) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            return new LoginResponse(
                    "ERROR",
                    "La cuenta se encuentra inactiva.",
                    null
            );
        }

        if (!"PACIENTE".equalsIgnoreCase(usuario.getRol())) {

            return new LoginResponse(
                    "ROL_NO_AUTORIZADO",
                    "Este acceso es exclusivo para pacientes. Si es personal del hospital, use el panel administrativo.",
                    usuario.getRol()
            );
        }

        return new LoginResponse(
                "OK",
                "Inicio de sesión exitoso.",
                usuario.getRol(),
                null,
                usuario.getNombreUsuario()
        );
    }


    // ==========================================
    // LOGIN DEL ADMINISTRADOR
    // ==========================================

    @Override
    public LoginResponse iniciarSesionAdmin(
            String nombreUsuario,
            String password) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOptional.isEmpty()) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }

        Usuario usuario = usuarioOptional.get();

        if (!usuario.getPassword().equals(password)) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            return new LoginResponse(
                    "ERROR",
                    "La cuenta se encuentra inactiva.",
                    null
            );
        }

        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {

            return new LoginResponse(
                    "ROL_NO_AUTORIZADO",
                    "Este acceso es exclusivo para administradores.",
                    usuario.getRol()
            );
        }

        return new LoginResponse(
                "OK",
                "Inicio de sesión administrativo exitoso.",
                usuario.getRol(),
                null,
                usuario.getNombreUsuario()
        );
    }
}