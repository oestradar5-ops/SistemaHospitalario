package com.hospital.his.service;

public interface CorreoService {

    void enviarBienvenida(
            String correo,
            String nombre
    );
}