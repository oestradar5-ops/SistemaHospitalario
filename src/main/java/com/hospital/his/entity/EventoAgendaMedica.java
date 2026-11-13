package com.hospital.his.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_agenda_medica")
public class EventoAgendaMedica {

    // ==========================================
    // IDENTIFICADOR DEL EVENTO
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // MÉDICO PROPIETARIO DEL EVENTO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    // ==========================================
    // INFORMACIÓN DEL EVENTO
    // ==========================================

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    // 0 = Reunión
    // 1 = Descanso
    // 2 = Capacitación
    // 3 = Personal
    // 4 = Otro

    @Column(name = "tipo_evento", nullable = false)
    private Integer tipoEvento;

    @Column(name = "todo_el_dia", nullable = false)
    private Boolean todoElDia = false;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public EventoAgendaMedica() {
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

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(Integer tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Boolean getTodoElDia() {
        return todoElDia;
    }

    public void setTodoElDia(Boolean todoElDia) {
        this.todoElDia = todoElDia;
    }
}