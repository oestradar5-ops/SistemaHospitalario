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
public class RecepcionLoginController {

    private final UsuarioRepository usuarioRepository;


    public RecepcionLoginController(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository =
                usuarioRepository;
    }


    @GetMapping("/recepcion/login")
    public String mostrarLogin() {

        return "recepcion/login";
    }


    @ResponseBody
    @PostMapping("/api/recepcion/login")
    public LoginResponse iniciarSesion(
            @RequestBody LoginRequest request,
            HttpSession session) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository
                        .findByNombreUsuario(
                                request.getNombreUsuario()
                        );


        if (usuarioOptional.isEmpty()) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }


        Usuario usuario =
                usuarioOptional.get();


        if (!usuario.getPassword()
                .equals(request.getPassword())) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }


        if (!Boolean.TRUE.equals(
                usuario.getActivo())) {

            return new LoginResponse(
                    "ERROR",
                    "La cuenta se encuentra inactiva.",
                    null
            );
        }


        if (!"RECEPCION".equalsIgnoreCase(
                usuario.getRol())) {

            return new LoginResponse(
                    "ROL_NO_AUTORIZADO",
                    "Este acceso es exclusivo para recepción.",
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


        return new LoginResponse(
                "OK",
                "Inicio de sesión exitoso.",
                usuario.getRol(),
                null,
                usuario.getNombreUsuario()
        );
    }


    @GetMapping("/recepcion/logout")
    public String cerrarSesion(
            HttpSession session) {

        session.invalidate();

        return "redirect:/recepcion/login";
    }
}