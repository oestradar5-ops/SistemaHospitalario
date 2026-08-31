package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_usuarios")
public class AuditoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String administrador;

    @Column(nullable = false, length = 30)
    private String accion;

    @Column(name = "usuario_afectado", length = 80)
    private String usuarioAfectado;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public AuditoriaUsuario() {
    }


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AuditoriaUsuario(
            String administrador,
            String accion,
            String usuarioAfectado,
            String descripcion) {

        this.administrador = administrador;
        this.accion = accion;
        this.usuarioAfectado = usuarioAfectado;
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }


    // ==========================================
    // ANTES DE GUARDAR
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
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


    public String getAdministrador() {
        return administrador;
    }

    public void setAdministrador(String administrador) {
        this.administrador = administrador;
    }


    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }


    public String getUsuarioAfectado() {
        return usuarioAfectado;
    }

    public void setUsuarioAfectado(String usuarioAfectado) {
        this.usuarioAfectado = usuarioAfectado;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}