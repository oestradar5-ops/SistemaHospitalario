package com.hospital.his.controller;

import com.hospital.his.dto.LoginRequest;
import com.hospital.his.dto.LoginResponse;
import com.hospital.his.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LaboratorioLoginController {

    // ==========================================
    // SERVICIO DE USUARIOS
    // ==========================================

    private final UsuarioService usuarioService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public LaboratorioLoginController(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }


    // ==========================================
    // MOSTRAR LOGIN DE LABORATORIO
    // CU-09
    // ==========================================

    @GetMapping("/laboratorio/login")
    public String mostrarLoginLaboratorio() {

        return "laboratorio/login-laboratorio";
    }


    // ==========================================
    // INICIAR SESIÓN DE LABORATORIO
    // CU-09
    // ==========================================

    @ResponseBody
    @PostMapping("/api/laboratorio/login")
    public LoginResponse iniciarSesionLaboratorio(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // ==========================================
        // VALIDAR CREDENCIALES
        // ==========================================

        LoginResponse respuesta =
                usuarioService.iniciarSesionLaboratorio(
                        request.getNombreUsuario(),
                        request.getPassword()
                );


        // ==========================================
        // VERIFICAR LOGIN CORRECTO
        // ==========================================

        if ("OK".equals(respuesta.getEstado())
                && "LABORATORIO".equalsIgnoreCase(
                respuesta.getRol())) {

            // ==========================================
            // INVALIDAR SESIÓN ANTERIOR
            // ==========================================

            HttpSession sesionAnterior =
                    httpRequest.getSession(false);

            if (sesionAnterior != null) {
                sesionAnterior.invalidate();
            }


            // ==========================================
            // CREAR NUEVA SESIÓN
            // ==========================================

            HttpSession nuevaSesion =
                    httpRequest.getSession(true);


            // ==========================================
            // GUARDAR USUARIO EN SESIÓN
            // ==========================================

            nuevaSesion.setAttribute(
                    "usuario",
                    respuesta.getNombreUsuario()
            );


            // ==========================================
            // GUARDAR ROL EN SESIÓN
            // ==========================================

            nuevaSesion.setAttribute(
                    "rol",
                    "LABORATORIO"
            );

        }

        return respuesta;
    }
}