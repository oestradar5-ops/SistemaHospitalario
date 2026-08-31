package com.hospital.his.service;

import com.hospital.his.dto.LoginResponse;
import com.hospital.his.dto.VerificacionDpiResponse;

public interface UsuarioService {

    VerificacionDpiResponse verificarDpi(String dpi);

    LoginResponse iniciarSesion(
            String nombreUsuario,
            String password
    );

    LoginResponse iniciarSesionAdmin(
            String nombreUsuario,
            String password
    );
}