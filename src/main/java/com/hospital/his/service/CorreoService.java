package com.hospital.his.service;

import com.hospital.his.entity.Cita;

public interface CorreoService {

    // ==========================================
    // CORREO DE BIENVENIDA
    // ==========================================

    void enviarBienvenida(
            String correo,
            String nombre
    );

    // ==========================================
    // CU-12 - CONFIRMACIÓN DE SEGUIMIENTO
    // ==========================================

    void enviarConfirmacionSeguimiento(
            Cita cita
    );

    // ==========================================
    // CU-12 - RECORDATORIO DE SEGUIMIENTO
    // ==========================================

    void enviarRecordatorioSeguimiento(
            Cita cita
    );
}