package com.hospital.his.entity;

import jakarta.persistence.*;

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
    // CONSTRUCTOR
    // ==========================================

    public OrdenExamen() {
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

    public OrdenLaboratorio getOrden() {
        return orden;
    }

    public void setOrden(OrdenLaboratorio orden) {
        this.orden = orden;
    }

    public CatalogoExamen getExamen() {
        return examen;
    }

    public void setExamen(CatalogoExamen examen) {
        this.examen = examen;
    }
}