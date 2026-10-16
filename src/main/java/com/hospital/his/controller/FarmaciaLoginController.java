package com.hospital.his.controller;

import com.hospital.his.dto.LoginRequest;
import com.hospital.his.dto.LoginResponse;
import com.hospital.his.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class FarmaciaLoginController {

    private final UsuarioService usuarioService;

    public FarmaciaLoginController(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }

    // ==========================================
    // MOSTRAR LOGIN DE FARMACIA
    // ==========================================

    @GetMapping("/farmacia/login")
    public String mostrarLogin() {

        return "farmacia/login-farmacia";
    }

    // ==========================================
    // INICIAR SESIÓN
    // ==========================================

    @PostMapping("/api/farmacia/login")
    @ResponseBody
    public LoginResponse iniciarSesion(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse respuesta =
                usuarioService.iniciarSesionFarmacia(
                        request.getNombreUsuario(),
                        request.getPassword()
                );

        if ("OK".equals(respuesta.getEstado())
                && "FARMACIA".equalsIgnoreCase(
                respuesta.getRol())) {

            HttpSession anterior =
                    httpRequest.getSession(false);

            if (anterior != null) {
                anterior.invalidate();
            }

            HttpSession nueva =
                    httpRequest.getSession(true);

            nueva.setAttribute(
                    "usuario",
                    respuesta.getNombreUsuario()
            );

            nueva.setAttribute(
                    "rol",
                    "FARMACIA"
            );
        }

        return respuesta;
    }

    // ==========================================
    // CU-11: CERRAR SESIÓN DE FARMACIA
    // ==========================================

    @PostMapping("/farmacia/logout")
    @ResponseBody
    public ResponseEntity<?> cerrarSesion(
            HttpServletRequest request) {

        HttpSession sesion = request.getSession(false);

        if (sesion == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error",
                            "No existe una sesión activa."
                    ));
        }

        Object rol = sesion.getAttribute("rol");

        if (!"FARMACIA".equalsIgnoreCase(
                String.valueOf(rol))) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error",
                            "Acceso no autorizado."
                    ));
        }

        sesion.invalidate();

        return ResponseEntity.ok(
                Map.of(
                        "mensaje",
                        "Sesión de Farmacia cerrada correctamente."
                )
        );
    }
}