package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
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
    // ESTADO DE LA ORDEN
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
    // ORDEN EXTERNA
    // CU-09
    // ==========================================

    @Column(
            name = "orden_externa",
            nullable = false
    )
    private Boolean ordenExterna = false;


    // ==========================================
    // MONTO TOTAL DE LA ORDEN
    // CU-09
    // ==========================================

    @Column(
            name = "monto_total",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal montoTotal = BigDecimal.ZERO;


    // ==========================================
    // FECHA DE PAGO
    // CU-09
    // ==========================================

    @Column(
            name = "fecha_pago"
    )
    private LocalDateTime fechaPago;


    // ==========================================
    // FECHA DE FINALIZACIÓN
    // CU-09
    // ==========================================

    @Column(
            name = "fecha_completada"
    )
    private LocalDateTime fechaCompletada;


    // ==========================================
    // EXÁMENES DE LA ORDEN
    // ==========================================

    @OneToMany(
            mappedBy = "orden",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrdenExamen> examenes = new ArrayList<>();


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public OrdenLaboratorio() {
    }


    // ==========================================
    // VALORES AUTOMÁTICOS
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null || estado.isBlank()) {
            estado = "PENDIENTE";
        }

        if (ordenExterna == null) {
            ordenExterna = false;
        }

        if (montoTotal == null) {
            montoTotal = BigDecimal.ZERO;
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
    // CITA
    // ==========================================

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }


    // ==========================================
    // OBSERVACIONES
    // ==========================================

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }


    // ==========================================
    // ESTADO
    // ==========================================

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    // ==========================================
    // FECHA DE CREACIÓN
    // ==========================================

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    // ==========================================
    // ORDEN EXTERNA
    // ==========================================

    public Boolean getOrdenExterna() {
        return ordenExterna;
    }

    public void setOrdenExterna(Boolean ordenExterna) {
        this.ordenExterna = ordenExterna;
    }


    // ==========================================
    // MONTO TOTAL
    // ==========================================

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }


    // ==========================================
    // FECHA DE PAGO
    // ==========================================

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }


    // ==========================================
    // FECHA COMPLETADA
    // ==========================================

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }

    public void setFechaCompletada(LocalDateTime fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }


    // ==========================================
    // LISTA DE EXÁMENES
    // ==========================================

    public List<OrdenExamen> getExamenes() {
        return examenes;
    }

    public void setExamenes(List<OrdenExamen> examenes) {
        this.examenes = examenes;
    }
}