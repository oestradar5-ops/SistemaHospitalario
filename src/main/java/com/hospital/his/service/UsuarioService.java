package com.hospital.his.service;

import com.hospital.his.dto.LoginResponse;
import com.hospital.his.dto.VerificacionDpiResponse;

public interface UsuarioService {

    // ==========================================
    // VERIFICACIÓN DE DPI
    // ==========================================

    VerificacionDpiResponse verificarDpi(String dpi);


    // ==========================================
    // LOGIN DEL PACIENTE
    // ==========================================

    LoginResponse iniciarSesion(
            String nombreUsuario,
            String password
    );


    // ==========================================
    // LOGIN DEL ADMINISTRADOR
    // ==========================================

    LoginResponse iniciarSesionAdmin(
            String nombreUsuario,
            String password
    );


    // ==========================================
    // LOGIN DEL PERSONAL DE LABORATORIO
    // CU-09
    // ==========================================

    LoginResponse iniciarSesionLaboratorio(
            String nombreUsuario,
            String password
    );


    // ==========================================
    // LOGIN DEL PERSONAL DE FARMACIA
    // CU-11
    // ==========================================

    LoginResponse iniciarSesionFarmacia(
            String nombreUsuario,
            String password
    );
}