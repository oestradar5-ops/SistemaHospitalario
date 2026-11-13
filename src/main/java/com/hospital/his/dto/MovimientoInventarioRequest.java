package com.hospital.his.dto;

import java.math.BigDecimal;

public class MovimientoInventarioRequest {

    // ==========================================
    // CU-15: DATOS DEL MOVIMIENTO
    // ==========================================

    private Long medicamentoId;

    private Long sucursalId;

    // 0 = Compra
    // 1 = Devolución
    // 2 = Venta
    // 3 = Reclamo
    // 4 = Ajuste+
    // 5 = Ajuste-
    // 6 = Despacho (solo automático)

    private Integer tipoMovimiento;

    private Integer cantidad;

    private BigDecimal costoUnitario;

    private String numeroReferencia;

    private String motivoNotas;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public MovimientoInventarioRequest() {
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Long medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Integer getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(Integer tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getNumeroReferencia() {
        return numeroReferencia;
    }

    public void setNumeroReferencia(String numeroReferencia) {
        this.numeroReferencia = numeroReferencia;
    }

    public String getMotivoNotas() {
        return motivoNotas;
    }

    public void setMotivoNotas(String motivoNotas) {
        this.motivoNotas = motivoNotas;
    }
}