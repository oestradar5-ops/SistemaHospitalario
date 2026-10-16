package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "despachos_medicamentos")
public class DespachoMedicamento {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // RECETA MÉDICA
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private RecetaMedica receta;

    // ==========================================
    // SUCURSAL
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    // ==========================================
    // USUARIO QUE REALIZÓ EL DESPACHO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ==========================================
    // FECHA
    // ==========================================

    @Column(name = "fecha_despacho", nullable = false)
    private LocalDateTime fechaDespacho;

    // ==========================================
    // TOTAL DEL DESPACHO
    // ==========================================

    @Column(
            name = "monto_total",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoTotal = BigDecimal.ZERO;

    // ==========================================
    // ESTADO
    // ==========================================

    @Column(nullable = false, length = 30)
    private String estado = "CONFIRMADO";

    // ==========================================
    // DETALLE DE MEDICAMENTOS DESPACHADOS
    // ==========================================

    @OneToMany(
            mappedBy = "despacho",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetalleDespacho> detalles = new ArrayList<>();

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DespachoMedicamento() {
    }

    // ==========================================
    // FECHA AUTOMÁTICA
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaDespacho == null) {
            fechaDespacho = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "CONFIRMADO";
        }

        if (montoTotal == null) {
            montoTotal = BigDecimal.ZERO;
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

    public RecetaMedica getReceta() {
        return receta;
    }

    public void setReceta(RecetaMedica receta) {
        this.receta = receta;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaDespacho() {
        return fechaDespacho;
    }

    public void setFechaDespacho(LocalDateTime fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetalleDespacho> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleDespacho> detalles) {
        this.detalles = detalles;
    }
}