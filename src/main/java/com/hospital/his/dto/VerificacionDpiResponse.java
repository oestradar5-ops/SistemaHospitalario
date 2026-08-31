package com.hospital.his.dto;

public class VerificacionDpiResponse {

    private boolean encontrado;
    private String tipo;
    private String mensaje;

    public VerificacionDpiResponse() {
    }

    public VerificacionDpiResponse(boolean encontrado, String tipo, String mensaje) {
        this.encontrado = encontrado;
        this.tipo = tipo;
        this.mensaje = mensaje;
    }

    public boolean isEncontrado() {
        return encontrado;
    }

    public void setEncontrado(boolean encontrado) {
        this.encontrado = encontrado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
