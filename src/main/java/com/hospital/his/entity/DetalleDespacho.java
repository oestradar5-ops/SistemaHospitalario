package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_despachos")
public class DetalleDespacho {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // DESPACHO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "despacho_id",
            nullable = false
    )
    private DespachoMedicamento despacho;


    // ==========================================
    // DETALLE ORIGINAL DE LA RECETA
    // CU-11: CONTROL DE DESPACHOS PARCIALES
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receta_medicamento_id",
            nullable = false
    )
    private RecetaMedicamento recetaMedicamento;


    // ==========================================
    // MEDICAMENTO ORIGINAL RECETADO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_recetado_id",
            nullable = false
    )
    private CatalogoMedicamento medicamentoRecetado;


    // ==========================================
    // MEDICAMENTO ENTREGADO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_entregado_id",
            nullable = false
    )
    private CatalogoMedicamento medicamentoEntregado;


    // ==========================================
    // CANTIDAD ENTREGADA
    // ==========================================

    @Column(nullable = false)
    private Integer cantidad;


    // ==========================================
    // PRECIO UNITARIO
    // ==========================================

    @Column(
            name = "precio_unitario",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precioUnitario;


    // ==========================================
    // SUBTOTAL
    // ==========================================

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;


    // ==========================================
    // INDICADOR DE SUSTITUCIÓN
    // ==========================================

    @Column(nullable = false)
    private Boolean sustituido = false;


    // ==========================================
    // RAZÓN DE SUSTITUCIÓN
    // ==========================================

    @Column(
            name = "razon_sustitucion",
            columnDefinition = "TEXT"
    )
    private String razonSustitucion;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public DetalleDespacho() {
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


    public DespachoMedicamento getDespacho() {
        return despacho;
    }

    public void setDespacho(
            DespachoMedicamento despacho) {

        this.despacho = despacho;
    }


    // ==========================================
    // GET Y SET DEL DETALLE DE RECETA
    // ==========================================

    public RecetaMedicamento getRecetaMedicamento() {
        return recetaMedicamento;
    }

    public void setRecetaMedicamento(
            RecetaMedicamento recetaMedicamento) {

        this.recetaMedicamento = recetaMedicamento;
    }


    public CatalogoMedicamento getMedicamentoRecetado() {
        return medicamentoRecetado;
    }

    public void setMedicamentoRecetado(
            CatalogoMedicamento medicamentoRecetado) {

        this.medicamentoRecetado = medicamentoRecetado;
    }


    public CatalogoMedicamento getMedicamentoEntregado() {
        return medicamentoEntregado;
    }

    public void setMedicamentoEntregado(
            CatalogoMedicamento medicamentoEntregado) {

        this.medicamentoEntregado = medicamentoEntregado;
    }


    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }


    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
            BigDecimal precioUnitario) {

        this.precioUnitario = precioUnitario;
    }


    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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