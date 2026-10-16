package com.hospital.his.dto;

public class FarmaciaDetalleRequest {

    // ==========================================
    // MEDICAMENTO DE LA RECETA
    // ==========================================

    private Long recetaMedicamentoId;

    // ==========================================
    // MEDICAMENTO QUE SE ENTREGARÁ
    // ==========================================

    private Long medicamentoEntregadoId;

    // ==========================================
    // CANTIDAD QUE SE ENTREGARÁ
    // ==========================================

    private Integer cantidad;

    // ==========================================
    // SUSTITUCIÓN
    // ==========================================

    private Boolean sustituido = false;

    // ==========================================
    // JUSTIFICACIÓN DE SUSTITUCIÓN
    // ==========================================

    private String razonSustitucion;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaDetalleRequest() {
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getRecetaMedicamentoId() {
        return recetaMedicamentoId;
    }

    public void setRecetaMedicamentoId(
            Long recetaMedicamentoId) {

        this.recetaMedicamentoId = recetaMedicamentoId;
    }

    public Long getMedicamentoEntregadoId() {
        return medicamentoEntregadoId;
    }

    public void setMedicamentoEntregadoId(
            Long medicamentoEntregadoId) {

        this.medicamentoEntregadoId = medicamentoEntregadoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Boolean getSustituido() {
        return sustituido;
    }

    public void setSustituido(Boolean sustituido) {
        this.sustituido = sustituido;
    }

    public String getRazonSustitucion() {
        return razonSustitucion;
    }

    public void setRazonSustitucion(
            String razonSustitucion) {

        this.razonSustitucion = razonSustitucion;
    }
}