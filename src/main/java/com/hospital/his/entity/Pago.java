package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(
            name = "cita_id",
            nullable = false,
            unique = true
    )
    private Cita cita;


    @Column(
            name = "monto",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal monto;


    @Column(
            name = "metodo_pago",
            nullable = false,
            length = 30
    )
    private String metodoPago;


    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado;


    @Column(
            name = "referencia",
            length = 100
    )
    private String referencia;


    @Column(
            name = "monto_recibido",
            precision = 10,
            scale = 2
    )
    private BigDecimal montoRecibido;


    @Column(
            name = "cambio",
            precision = 10,
            scale = 2
    )
    private BigDecimal cambio;


    @Column(
            name = "fecha_pago",
            nullable = false
    )
    private LocalDateTime fechaPago;


    public Pago() {
    }


    @PrePersist
    public void prePersist() {

        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }


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


    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }


    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }


    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(
            BigDecimal montoRecibido) {

        this.montoRecibido = montoRecibido;
    }


    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(
            BigDecimal cambio) {

        this.cambio = cambio;
    }


    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(
            LocalDateTime fechaPago) {

        this.fechaPago = fechaPago;
    }
}