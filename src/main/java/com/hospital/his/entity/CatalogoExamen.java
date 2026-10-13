package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_examenes")
public class CatalogoExamen {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // NOMBRE DEL EXAMEN
    // ==========================================

    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 150
    )
    private String nombre;


    // ==========================================
    // DESCRIPCIÓN
    // ==========================================

    @Column(
            name = "descripcion",
            columnDefinition = "TEXT"
    )
    private String descripcion;


    // ==========================================
    // ESTADO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoExamen() {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}