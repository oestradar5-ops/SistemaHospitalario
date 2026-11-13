package com.hospital.his.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas_agenda_medica")
public class TareaAgendaMedica {

    // ==========================================
    // IDENTIFICADOR DE LA TAREA
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // MÉDICO PROPIETARIO DE LA TAREA
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;


    // ==========================================
    // INFORMACIÓN DE LA TAREA
    // ==========================================

    @Column(nullable = false, length = 200)
    private String titulo;


    @Column(length = 1000)
    private String descripcion;


    // 0 = Baja
    // 1 = Normal
    // 2 = Alta

    @Column(name = "prioridad", nullable = false)
    private Integer prioridad = 1;


    @Column(name = "fecha_limite", nullable = false)
    private LocalDateTime fechaLimite;


    // false = Pendiente
    // true = Completada

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public TareaAgendaMedica() {
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


    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
    }


    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }


    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}