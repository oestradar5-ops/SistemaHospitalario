package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rechazos_medicamentos")
public class RechazoMedicamento {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // RECETA MÉDICA
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private RecetaMedica receta;

    // ==========================================
    // SUCURSAL
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    // ==========================================
    // USUARIO QUE REGISTRA EL RECHAZO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ==========================================
    // MOTIVO DEL RECHAZO
    // ==========================================

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    // ==========================================
    // ESTADO
    // ==========================================

    @Column(nullable = false, length = 30)
    private String estado = "RECHAZADO";

    // ==========================================
    // FECHA DEL RECHAZO
    // ==========================================

    @Column(name = "fecha_rechazo", nullable = false)
    private LocalDateTime fechaRechazo;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public RechazoMedicamento() {
    }

    // ==========================================
    // FECHA AUTOMÁTICA
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaRechazo == null) {
            fechaRechazo = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "RECHAZADO";
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

    public RecetaMedica getReceta() {
        return receta;
    }

    public void setReceta(RecetaMedica receta) {
        this.receta = receta;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRechazo() {
        return fechaRechazo;
    }

    public void setFechaRechazo(LocalDateTime fechaRechazo) {
        this.fechaRechazo = fechaRechazo;
    }
}