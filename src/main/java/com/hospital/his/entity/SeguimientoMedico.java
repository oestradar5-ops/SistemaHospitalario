package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguimientos_medicos")
public class SeguimientoMedico {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // CITA MÉDICA
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    // ==========================================
    // FECHA DE SEGUIMIENTO
    // ==========================================

    @Column(
            name = "fecha_seguimiento",
            nullable = false
    )
    private LocalDate fechaSeguimiento;


    // ==========================================
    // MOTIVO
    // ==========================================

    @Column(
            name = "motivo",
            nullable = false,
            length = 250
    )
    private String motivo;


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
    private String estado = "PROGRAMADO";


    // ==========================================
    // FECHA DE CREACIÓN
    // ==========================================

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaCreacion;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public SeguimientoMedico() {
    }


    // ==========================================
    // FECHA AUTOMÁTICA
    // ==========================================

    @PrePersist
    public void antesDeGuardar() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null || estado.isBlank()) {
            estado = "PROGRAMADO";
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

    public LocalDate getFechaSeguimiento() {
        return fechaSeguimiento;
    }

    public void setFechaSeguimiento(LocalDate fechaSeguimiento) {
        this.fechaSeguimiento = fechaSeguimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
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
}