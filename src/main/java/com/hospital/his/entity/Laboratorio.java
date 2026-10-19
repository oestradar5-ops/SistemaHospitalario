package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_laboratorios")
public class Laboratorio {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // NOMBRE DEL LABORATORIO
    // ==========================================

    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 120
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
    // UBICACIÓN
    // ==========================================

    @Column(
            name = "ubicacion",
            length = 200
    )
    private String ubicacion;


    // ==========================================
    // TELÉFONO
    // ==========================================

    @Column(
            name = "telefono",
            length = 8
    )
    private String telefono;


    // ==========================================
    // ESTADO ACTIVO / INACTIVO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public Laboratorio() {
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


    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }


    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }


    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}