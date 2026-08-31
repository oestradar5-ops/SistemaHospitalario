package com.hospital.his.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "signos_vitales")
public class SignosVitales {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // CITA
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    // ==========================================
    // ENFERMERO
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "enfermero_id",
            nullable = false
    )
    private Usuario enfermero;


    // ==========================================
    // SIGNOS VITALES
    // ==========================================

    @Column(
            name = "presion_sistolica",
            nullable = false
    )
    private Integer presionSistolica;


    @Column(
            name = "presion_diastolica",
            nullable = false
    )
    private Integer presionDiastolica;


    @Column(
            nullable = false,
            precision = 4,
            scale = 1
    )
    private BigDecimal temperatura;


    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal peso;


    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal talla;


    @Column(
            name = "frecuencia_cardiaca",
            nullable = false
    )
    private Integer frecuenciaCardiaca;


    // ==========================================
    // EMERGENCIA Y ALERTA
    // ==========================================

    @Column(
            name = "es_emergencia",
            nullable = false
    )
    private Boolean esEmergencia = false;


    @Column(
            name = "alerta_clinica",
            nullable = false
    )
    private Boolean alertaClinica = false;


    // ==========================================
    // FECHA
    // ==========================================

    @Column(
            name = "fecha_registro",
            nullable = false
    )
    private LocalDateTime fechaRegistro;


    public SignosVitales() {
    }


    @PrePersist
    public void prePersist() {

        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }

        if (esEmergencia == null) {
            esEmergencia = false;
        }

        if (alertaClinica == null) {
            alertaClinica = false;
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


    public Usuario getEnfermero() {
        return enfermero;
    }

    public void setEnfermero(Usuario enfermero) {
        this.enfermero = enfermero;
    }


    public Integer getPresionSistolica() {
        return presionSistolica;
    }

    public void setPresionSistolica(
            Integer presionSistolica) {

        this.presionSistolica = presionSistolica;
    }


    public Integer getPresionDiastolica() {
        return presionDiastolica;
    }

    public void setPresionDiastolica(
            Integer presionDiastolica) {

        this.presionDiastolica = presionDiastolica;
    }


    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(
            BigDecimal temperatura) {

        this.temperatura = temperatura;
    }


    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(
            BigDecimal peso) {

        this.peso = peso;
    }


    public BigDecimal getTalla() {
        return talla;
    }

    public void setTalla(
            BigDecimal talla) {

        this.talla = talla;
    }


    public Integer getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(
            Integer frecuenciaCardiaca) {

        this.frecuenciaCardiaca =
                frecuenciaCardiaca;
    }


    public Boolean getEsEmergencia() {
        return esEmergencia;
    }

    public void setEsEmergencia(
            Boolean esEmergencia) {

        this.esEmergencia = esEmergencia;
    }


    public Boolean getAlertaClinica() {
        return alertaClinica;
    }

    public void setAlertaClinica(
            Boolean alertaClinica) {

        this.alertaClinica = alertaClinica;
    }


    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDateTime fechaRegistro) {

        this.fechaRegistro = fechaRegistro;
    }
}