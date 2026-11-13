package com.hospital.his.interceptor;

import com.hospital.his.config.DisponibilidadSistemaConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Locale;

@Component
public class DisponibilidadInterceptor implements HandlerInterceptor {

    private final DisponibilidadSistemaConfig disponibilidad;

    public DisponibilidadInterceptor(
            DisponibilidadSistemaConfig disponibilidad) {

        this.disponibilidad = disponibilidad;
    }

    // ==========================================
    // VERIFICAR DISPONIBILIDAD
    // ==========================================

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        String ruta = request.getRequestURI()
                .substring(request.getContextPath().length());

        String modulo = identificarModulo(ruta);

        // ==========================================
        // MANTENIMIENTO GENERAL
        // ==========================================

        if (disponibilidad.isMantenimientoGeneral()) {

            enviarMensaje(
                    response,
                    disponibilidad.mensajeMantenimientoGeneral()
            );

            return false;
        }

        // ==========================================
        // MANTENIMIENTO POR MÓDULO
        // ==========================================

        if (modulo != null &&
                !disponibilidad.moduloDisponible(modulo)) {

            enviarMensaje(
                    response,
                    disponibilidad.mensajeModuloNoDisponible(modulo)
            );

            return false;
        }

        return true;
    }

    // ==========================================
    // IDENTIFICAR MÓDULO
    // ==========================================

    private String identificarModulo(String ruta) {

        String r = ruta.toLowerCase(Locale.ROOT);

        // ==========================================
        // PORTAL Y PANEL PRINCIPAL
        // ==========================================

        if (r.equals("/")
                || coincide(r, "/portal")
                || coincide(r, "/panel")) {

            return "portal";
        }

        // ==========================================
        // ADMINISTRACIÓN
        // ==========================================

        if (coincide(r, "/admin")
                || coincide(r, "/api/admin")
                || coincide(r, "/branch-specialty")) {

            return "administracion";
        }

        // ==========================================
        // PACIENTES
        // ==========================================

        if (coincide(r, "/paciente")
                || coincide(r, "/login")
                || coincide(r, "/logout")
                || coincide(r, "/registro")
                || coincide(r, "/api/login")
                || coincide(r, "/api/registro")
                || coincide(r, "/api/verificar-dpi")
                || coincide(r, "/api/citas")
                || coincide(r, "/api/pagos")) {

            return "pacientes";
        }

        // ==========================================
        // MÉDICOS
        // ==========================================

        if (coincide(r, "/medico")
                || coincide(r, "/api/medico")) {

            return "medicos";
        }

        // ==========================================
        // ENFERMERÍA
        // ==========================================

        if (coincide(r, "/enfermeria")
                || coincide(r, "/api/enfermeria")) {

            return "enfermeria";
        }

        // ==========================================
        // RECEPCIÓN
        // ==========================================

        if (coincide(r, "/recepcion")
                || coincide(r, "/api/recepcion")) {

            return "recepcion";
        }

        // ==========================================
        // FARMACIA
        // ==========================================

        if (coincide(r, "/farmacia")
                || coincide(r, "/api/farmacia")) {

            return "farmacia";
        }

        // ==========================================
        // LABORATORIO
        // ==========================================

        if (coincide(r, "/laboratorio")
                || coincide(r, "/api/laboratorio")) {

            return "laboratorio";
        }

        // ==========================================
        // CAJA
        // ==========================================

        if (coincide(r, "/caja")
                || coincide(r, "/api/caja")) {

            return "caja";
        }

        return null;
    }

    // ==========================================
    // COMPROBAR PREFIJO COMPLETO
    // ==========================================

    private boolean coincide(String ruta, String prefijo) {

        return ruta.equals(prefijo)
                || ruta.startsWith(prefijo + "/");
    }

    // ==========================================
    // ENVIAR MENSAJE DE MANTENIMIENTO
    // ==========================================

    private void enviarMensaje(
            HttpServletResponse response,
            String mensaje) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE
        );

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "text/plain;charset=UTF-8"
        );

        response.getWriter().write(mensaje);
    }
}