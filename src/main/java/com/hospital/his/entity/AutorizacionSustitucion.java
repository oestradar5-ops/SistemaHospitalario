package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "autorizaciones_sustitucion")
public class AutorizacionSustitucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Medicamento originalmente recetado
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receta_medicamento_id", nullable = false)
    private RecetaMedicamento recetaMedicamento;

    // Medicamento que se desea entregar
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_sustituto_id", nullable = false)
    private CatalogoMedicamento medicamentoSustituto;

    // Empleado de Farmacia que solicita el cambio
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitado_por_id", nullable = false)
    private Usuario solicitadoPor;

    // Médico que responde a la solicitud
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizado_por_id")
    private Usuario autorizadoPor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(length = 1000)
    private String observacionesMedico;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaRespuesta;

    @PrePersist
    public void prePersist() {

        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    public Long getId() {
        return id;
    }

    public RecetaMedicamento getRecetaMedicamento() {
        return recetaMedicamento;
    }

    public void setRecetaMedicamento(
            RecetaMedicamento recetaMedicamento) {
        this.recetaMedicamento = recetaMedicamento;
    }

    public CatalogoMedicamento getMedicamentoSustituto() {
        return medicamentoSustituto;
    }

    public void setMedicamentoSustituto(
            CatalogoMedicamento medicamentoSustituto) {
        this.medicamentoSustituto = medicamentoSustituto;
    }

    public Usuario getSolicitadoPor() {
        return solicitadoPor;
    }

    public void setSolicitadoPor(Usuario solicitadoPor) {
        this.solicitadoPor = solicitadoPor;
    }

    public Usuario getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(Usuario autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacionesMedico() {
        return observacionesMedico;
    }

    public void setObservacionesMedico(
            String observacionesMedico) {
        this.observacionesMedico = observacionesMedico;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(
            LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(
            LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}