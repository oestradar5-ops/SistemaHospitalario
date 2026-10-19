package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

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
    // ESTADO ACTIVO / INACTIVO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // PRECIO DEL EXAMEN
    // CU-09
    // ==========================================

    @Column(
            name = "precio",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precio = BigDecimal.ZERO;


    // ==========================================
    // RANGO DE REFERENCIA
    // CU-09
    // ==========================================

    @Column(
            name = "rango_referencia",
            length = 150
    )
    private String rangoReferencia;


    // ==========================================
    // UNIDAD DE REFERENCIA
    // CU-09
    // ==========================================

    @Column(
            name = "unidad_referencia",
            length = 50
    )
    private String unidadReferencia;


    // ==========================================
    // CU-14
    // LABORATORIO ASOCIADO AL EXAMEN
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "laboratorio_id",
            referencedColumnName = "id",
            nullable = true
    )
    private Laboratorio laboratorio;


    // ==========================================
    // CONSTRUCTOR VACÍO
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


    // ==========================================
    // PRECIO
    // ==========================================

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }


    // ==========================================
    // RANGO DE REFERENCIA
    // ==========================================

    public String getRangoReferencia() {
        return rangoReferencia;
    }

    public void setRangoReferencia(String rangoReferencia) {
        this.rangoReferencia = rangoReferencia;
    }


    // ==========================================
    // UNIDAD DE REFERENCIA
    // ==========================================

    public String getUnidadReferencia() {
        return unidadReferencia;
    }

    public void setUnidadReferencia(String unidadReferencia) {
        this.unidadReferencia = unidadReferencia;
    }


    // ==========================================
    // CU-14
    // LABORATORIO
    // ==========================================

    public Laboratorio getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }
}