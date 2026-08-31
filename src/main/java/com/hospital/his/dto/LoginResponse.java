package com.hospital.his.dto;

public class LoginResponse {

    private String estado;
    private String mensaje;
    private String rol;
    private Long segundosBloqueo;
    private String nombreUsuario;

    public LoginResponse() {
    }

    public LoginResponse(
            String estado,
            String mensaje,
            String rol) {

        this.estado = estado;
        this.mensaje = mensaje;
        this.rol = rol;
    }

    public LoginResponse(
            String estado,
            String mensaje,
            String rol,
            Long segundosBloqueo) {

        this.estado = estado;
        this.mensaje = mensaje;
        this.rol = rol;
        this.segundosBloqueo = segundosBloqueo;
    }

    public LoginResponse(
            String estado,
            String mensaje,
            String rol,
            Long segundosBloqueo,
            String nombreUsuario) {

        this.estado = estado;
        this.mensaje = mensaje;
        this.rol = rol;
        this.segundosBloqueo = segundosBloqueo;
        this.nombreUsuario = nombreUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getSegundosBloqueo() {
        return segundosBloqueo;
    }

    public void setSegundosBloqueo(Long segundosBloqueo) {
        this.segundosBloqueo = segundosBloqueo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}