package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "orden_examenes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orden_examen",
                        columnNames = {
                                "orden_id",
                                "examen_id"
                        }
                )
        }
)
public class OrdenExamen {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // ORDEN DE LABORATORIO
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "orden_id",
            nullable = false
    )
    private OrdenLaboratorio orden;


    // ==========================================
    // EXAMEN SELECCIONADO
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "examen_id",
            nullable = false
    )
    private CatalogoExamen examen;


    // ==========================================
    // PRECIO INDIVIDUAL DEL EXAMEN
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
    // VALOR DEL RESULTADO
    // CU-09
    // ==========================================

    @Column(
            name = "valor_resultado",
            length = 250
    )
    private String valorResultado;


    // ==========================================
    // UNIDAD DEL RESULTADO
    // CU-09
    // ==========================================

    @Column(
            name = "unidad",
            length = 50
    )
    private String unidad;


    // ==========================================
    // FECHA DEL RESULTADO
    // CU-09
    // ==========================================

    @Column(
            name = "fecha_resultado"
    )
    private LocalDateTime fechaResultado;


    // ==========================================
    // RESULTADO FUERA DE RANGO
    // CU-09 - FA02
    // ==========================================

    @Column(
            name = "fuera_rango",
            nullable = false
    )
    private Boolean fueraRango = false;


    // ==========================================
    // NOTAS DEL RESULTADO
    // CU-09
    // ==========================================

    @Column(
            name = "notas_resultado",
            columnDefinition = "TEXT"
    )
    private String notasResultado;


    // ==========================================
    // RESULTADO PUBLICADO
    // CU-09
    // ==========================================

    @Column(
            name = "publicado",
            nullable = false
    )
    private Boolean publicado = false;


    // ==========================================
    // FECHA DE PUBLICACIÓN
    // CU-09
    // ==========================================

    @Column(
            name = "fecha_publicacion"
    )
    private LocalDateTime fechaPublicacion;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public OrdenExamen() {
    }


    // ==========================================
    // VALORES AUTOMÁTICOS
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (precio == null) {
            precio = BigDecimal.ZERO;
        }

        if (fueraRango == null) {
            fueraRango = false;
        }

        if (publicado == null) {
            publicado = false;
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


    // ==========================================
    // ORDEN DE LABORATORIO
    // ==========================================

    public OrdenLaboratorio getOrden() {
        return orden;
    }

    public void setOrden(OrdenLaboratorio orden) {
        this.orden = orden;
    }


    // ==========================================
    // EXAMEN
    // ==========================================

    public CatalogoExamen getExamen() {
        return examen;
    }

    public void setExamen(CatalogoExamen examen) {
        this.examen = examen;
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
    // VALOR DEL RESULTADO
    // ==========================================

    public String getValorResultado() {
        return valorResultado;
    }

    public void setValorResultado(String valorResultado) {
        this.valorResultado = valorResultado;
    }


    // ==========================================
    // UNIDAD
    // ==========================================

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }


    // ==========================================
    // FECHA DEL RESULTADO
    // ==========================================

    public LocalDateTime getFechaResultado() {
        return fechaResultado;
    }

    public void setFechaResultado(LocalDateTime fechaResultado) {
        this.fechaResultado = fechaResultado;
    }


    // ==========================================
    // FUERA DE RANGO
    // ==========================================

    public Boolean getFueraRango() {
        return fueraRango;
    }

    public void setFueraRango(Boolean fueraRango) {
        this.fueraRango = fueraRango;
    }


    // ==========================================
    // NOTAS DEL RESULTADO
    // ==========================================

    public String getNotasResultado() {
        return notasResultado;
    }

    public void setNotasResultado(String notasResultado) {
        this.notasResultado = notasResultado;
    }


    // ==========================================
    // PUBLICADO
    // ==========================================

    public Boolean getPublicado() {
        return publicado;
    }

    public void setPublicado(Boolean publicado) {
        this.publicado = publicado;
    }


    // ==========================================
    // FECHA DE PUBLICACIÓN
    // ==========================================

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
}