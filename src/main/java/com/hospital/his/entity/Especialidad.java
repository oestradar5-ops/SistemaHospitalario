package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidades")
public class Especialidad {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // NOMBRE DE LA ESPECIALIDAD
    // ==========================================

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    // ==========================================
    // DESCRIPCIÓN - CU-14
    // ==========================================

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // ==========================================
    // ESTADO DE LA ESPECIALIDAD
    // ==========================================

    @Column(nullable = false)
    private Boolean activo = true;

    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public Especialidad() {
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

    // ==========================================
    // DESCRIPCIÓN
    // ==========================================

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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