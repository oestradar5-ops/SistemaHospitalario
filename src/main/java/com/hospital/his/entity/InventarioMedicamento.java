package com.hospital.his.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "inventario_medicamentos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventario_medicamento_sucursal",
                        columnNames = {"medicamento_id", "sucursal_id"}
                )
        }
)
public class InventarioMedicamento {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // MEDICAMENTO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_id",
            nullable = false
    )
    private CatalogoMedicamento medicamento;

    // ==========================================
    // SUCURSAL
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sucursal_id",
            nullable = false
    )
    private Sucursal sucursal;

    // ==========================================
    // CANTIDAD DISPONIBLE
    // ==========================================

    @Column(
            name = "cantidad_disponible",
            nullable = false
    )
    private Integer cantidadDisponible = 0;

    // ==========================================
    // STOCK MÍNIMO
    // ==========================================

    @Column(
            name = "stock_minimo",
            nullable = false
    )
    private Integer stockMinimo = 5;

    // ==========================================
    // PRECIO UNITARIO
    // ==========================================

    @Column(
            name = "precio_unitario",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    // ==========================================
    // ESTADO
    // ==========================================

    @Column(nullable = false)
    private Boolean activo = true;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public InventarioMedicamento() {
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

    public CatalogoMedicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(CatalogoMedicamento medicamento) {
        this.medicamento = medicamento;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}