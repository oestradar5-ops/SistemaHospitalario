package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_laboratorio")
public class OrdenLaboratorio {

    // ==========================================
    // ID DE LA ORDEN
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // CITA MÉDICA
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    // ==========================================
    // OBSERVACIONES
    // ==========================================

    @Column(
            name = "observaciones",
            columnDefinition = "TEXT"
    )
    private String observaciones;


    // ==========================================
    // ESTADO
    // ==========================================

    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado = "PENDIENTE";


    // ==========================================
    // FECHA DE CREACIÓN
    // ==========================================

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    // ==========================================
    // EXÁMENES DE LA ORDEN
    // ==========================================

    @OneToMany(
            mappedBy = "orden",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrdenExamen> examenes =
            new ArrayList<>();


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public OrdenLaboratorio() {
    }


    // ==========================================
    // FECHA AUTOMÁTICA
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null || estado.isBlank()) {
            estado = "PENDIENTE";
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

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<OrdenExamen> getExamenes() {
        return examenes;
    }

    public void setExamenes(List<OrdenExamen> examenes) {
        this.examenes = examenes;
    }
}