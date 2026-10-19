package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_estados_cita")
public class EstadoCita {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // CÓDIGO DEL ESTADO
    // ==========================================

    @Column(
            name = "codigo",
            nullable = false,
            unique = true,
            length = 50
    )
    private String codigo;

    // ==========================================
    // NOMBRE DEL ESTADO
    // ==========================================

    @Column(
            name = "nombre",
            nullable = false,
            length = 100
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
    // ESTADO ACTIVO / INACTIVO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;

    // ==========================================
    // ESTADO PROTEGIDO
    // ==========================================

    @Column(
            name = "protegido",
            nullable = false
    )
    private Boolean protegido = false;

    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public EstadoCita() {
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Boolean getProtegido() {
        return protegido;
    }

    public void setProtegido(Boolean protegido) {
        this.protegido = protegido;
    }
}