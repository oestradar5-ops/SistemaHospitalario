package com.hospital.his.controller;

import com.hospital.his.dto.LoginRequest;
import com.hospital.his.dto.LoginResponse;
import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
public class EnfermeriaLoginController {

    private final UsuarioRepository usuarioRepository;


    public EnfermeriaLoginController(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }


    // ==========================================
    // MOSTRAR LOGIN
    // ==========================================

    @GetMapping("/enfermeria/login")
    public String mostrarLogin() {

        return "enfermeria/login";
    }


    // ==========================================
    // INICIAR SESIÓN
    // ==========================================

    @ResponseBody
    @PostMapping("/api/enfermeria/login")
    public LoginResponse iniciarSesion(
            @RequestBody LoginRequest request,
            HttpSession session) {


        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByNombreUsuario(
                        request.getNombreUsuario()
                );


        if (usuarioOptional.isEmpty()) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }


        Usuario usuario = usuarioOptional.get();


        if (!usuario.getPassword()
                .equals(request.getPassword())) {

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


        if (!"ENFERMERO".equalsIgnoreCase(
                usuario.getRol())) {

            return new LoginResponse(
                    "ROL_NO_AUTORIZADO",
                    "Este acceso es exclusivo para Enfermería.",
                    usuario.getRol()
            );
        }


        session.setAttribute(
                "usuario",
                usuario.getNombreUsuario()
        );

        session.setAttribute(
                "rol",
                usuario.getRol()
        );

        session.setAttribute(
                "usuarioId",
                usuario.getId()
        );


        return new LoginResponse(
                "OK",
                "Inicio de sesión exitoso.",
                usuario.getRol(),
                null,
                usuario.getNombreUsuario()
        );
    }


    // ==========================================
    // CERRAR SESIÓN
    // ==========================================

    @GetMapping("/enfermeria/logout")
    public String cerrarSesion(
            HttpSession session) {

        session.invalidate();

        return "redirect:/enfermeria/login";
    }
}