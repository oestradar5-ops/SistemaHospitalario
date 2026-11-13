package com.hospital.his.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DisponibilidadSistemaConfig {

    // ==========================================
    // MANTENIMIENTO GENERAL
    // ==========================================

    @Value("${hospital.mantenimiento.general:false}")
    private boolean mantenimientoGeneral;


    // ==========================================
    // DISPONIBILIDAD DE MÓDULOS
    // ==========================================

    @Value("${hospital.modulos.portal:true}")
    private boolean portal;

    @Value("${hospital.modulos.pacientes:true}")
    private boolean pacientes;

    @Value("${hospital.modulos.administracion:true}")
    private boolean administracion;

    @Value("${hospital.modulos.medicos:true}")
    private boolean medicos;

    @Value("${hospital.modulos.recepcion:true}")
    private boolean recepcion;

    @Value("${hospital.modulos.farmacia:true}")
    private boolean farmacia;

    @Value("${hospital.modulos.laboratorio:true}")
    private boolean laboratorio;

    @Value("${hospital.modulos.caja:true}")
    private boolean caja;

    @Value("${hospital.modulos.enfermeria:true}")
    private boolean enfermeria;

    // ==========================================
    // CONSULTAR MANTENIMIENTO GENERAL
    // ==========================================

    public boolean isMantenimientoGeneral() {
        return mantenimientoGeneral;
    }


    // ==========================================
    // CONSULTAR DISPONIBILIDAD DE UN MÓDULO
    // ==========================================

    public boolean moduloDisponible(String modulo) {

        if (modulo == null) {
            return false;
        }

        return switch (modulo.toLowerCase()) {

            case "portal" -> portal;

            case "pacientes" -> pacientes;

            case "administracion" -> administracion;

            case "medicos" -> medicos;

            case "recepcion" -> recepcion;

            case "farmacia" -> farmacia;

            case "laboratorio" -> laboratorio;

            case "caja" -> caja;

            case "enfermeria" -> enfermeria;

            default -> false;
        };
    }


    // ==========================================
    // MENSAJE DE MANTENIMIENTO GENERAL
    // ==========================================

    public String mensajeMantenimientoGeneral() {

        return "El sistema se encuentra en mantenimiento. " +
                "Por favor, intente más tarde. " +
                "Disculpe las molestias.";
    }


    // ==========================================
    // MENSAJE DE MÓDULO NO DISPONIBLE
    // ==========================================

    public String mensajeModuloNoDisponible(String modulo) {

        return "El módulo " + modulo +
                " no está disponible temporalmente. " +
                "Los demás servicios continúan operando.";
    }
}