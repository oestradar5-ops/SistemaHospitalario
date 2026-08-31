package com.hospital.his.controller;

import com.hospital.his.dto.LoginRequest;
import com.hospital.his.dto.LoginResponse;
import com.hospital.his.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminLoginController {

    private final UsuarioService usuarioService;

    public AdminLoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // MOSTRAR LOGIN ADMINISTRATIVO
    // ==========================================

    @GetMapping("/admin/login")
    public String mostrarLoginAdmin() {

        return "admin/login";
    }

    // ==========================================
    // PROCESAR LOGIN ADMINISTRATIVO
    // ==========================================

    @ResponseBody
    @PostMapping("/api/admin/login")
    public LoginResponse iniciarSesionAdmin(
            @RequestBody LoginRequest request,
            HttpSession session) {

        LoginResponse respuesta =
                usuarioService.iniciarSesionAdmin(
                        request.getNombreUsuario(),
                        request.getPassword()
                );

        // Si las credenciales son correctas,
        // guardamos el usuario y el rol en la sesión.

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

    // ==========================================
    // CERRAR SESIÓN ADMINISTRATIVA
    // ==========================================

    @GetMapping("/admin/logout")
    public String cerrarSesionAdmin(
            HttpSession session) {

        session.invalidate();

        return "redirect:/admin/login";
    }
}
