package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_medicamentos")
public class CatalogoMedicamento {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // NOMBRE DEL MEDICAMENTO
    // ==========================================

    @Column(
            name = "nombre",
            nullable = false,
            length = 150
    )
    private String nombre;


    // ==========================================
    // PRESENTACIÓN
    // ==========================================

    @Column(
            name = "presentacion",
            length = 100
    )
    private String presentacion;


    // ==========================================
    // CONCENTRACIÓN
    // ==========================================

    @Column(
            name = "concentracion",
            length = 100
    )
    private String concentracion;


    // ==========================================
    // CU-14: UNIDAD DE MEDIDA
    // ==========================================

    @Column(
            name = "unidad_medida",
            length = 50
    )
    private String unidadMedida;


    // ==========================================
    // CU-14: MEDICAMENTO CONTROLADO
    // ==========================================

    @Column(
            name = "controlado",
            nullable = false
    )
    private Boolean controlado = false;


    // ==========================================
    // ESTADO DEL MEDICAMENTO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoMedicamento() {
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


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }


    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }


    // ==========================================
    // CU-14: UNIDAD DE MEDIDA
    // ==========================================

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }


    // ==========================================
    // CU-14: MEDICAMENTO CONTROLADO
    // ==========================================

    public Boolean getControlado() {
        return controlado;
    }

    public void setControlado(Boolean controlado) {
        this.controlado = controlado;
    }


    // ==========================================
    // ESTADO
    // ==========================================

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

}