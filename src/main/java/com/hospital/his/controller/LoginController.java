package com.hospital.his.controller;

import com.hospital.his.dto.LoginRequest;
import com.hospital.his.dto.LoginResponse;
import com.hospital.his.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login/login";
    }

    @ResponseBody
    @PostMapping("/api/login")
    public LoginResponse iniciarSesion(
            @RequestBody LoginRequest request,
            HttpSession session) {

        LoginResponse respuesta =
                usuarioService.iniciarSesion(
                        request.getNombreUsuario(),
                        request.getPassword()
                );

        if ("OK".equals(respuesta.getEstado())) {

            session.setAttribute(
                    "usuario",
                    respuesta.getNombreUsuario()
            );

            session.setAttribute(
                    "rol",
                    respuesta.getRol()
            );
        }

        return respuesta;
    }
    @GetMapping("/logout")
    public String cerrarSesion(
            HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}