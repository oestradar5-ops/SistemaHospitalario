package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "receta_medicamentos")
public class RecetaMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private RecetaMedica receta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private CatalogoMedicamento medicamento;

    @Column(nullable = false, length = 100)
    private String dosis;

    @Column(nullable = false, length = 100)
    private String frecuencia;

    @Column(nullable = false, length = 100)
    private String duracion;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(columnDefinition = "TEXT")
    private String indicaciones;

    public RecetaMedicamento() {
    }

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

    public CatalogoMedicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(CatalogoMedicamento medicamento) {
        this.medicamento = medicamento;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }
}