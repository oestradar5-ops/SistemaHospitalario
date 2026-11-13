package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // INVENTARIO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioMedicamento inventario;

    // ==========================================
    // TIPO DE MOVIMIENTO
    // ==========================================

    @Column(name = "tipo_movimiento", nullable = false)
    private Integer tipoMovimiento;

    // ==========================================
    // CANTIDAD Y EXISTENCIAS
    // ==========================================

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    // ==========================================
    // COSTO Y REFERENCIA
    // ==========================================

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "numero_referencia", length = 100)
    private String numeroReferencia;

    @Column(name = "motivo_notas", length = 500)
    private String motivoNotas;

    // ==========================================
    // USUARIO RESPONSABLE
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ==========================================
    // FECHA Y ESTADO
    // ==========================================

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private Boolean activo = true;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public MovimientoInventario() {
    }

    // ==========================================
    // FECHA AUTOMÁTICA
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }

        if (activo == null) {
            activo = true;
        }
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InventarioMedicamento getInventario() {
        return inventario;
    }

    public void setInventario(InventarioMedicamento inventario) {
        this.inventario = inventario;
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

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}