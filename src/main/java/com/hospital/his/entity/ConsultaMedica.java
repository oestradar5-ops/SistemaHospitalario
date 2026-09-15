package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas_medicas")
public class ConsultaMedica {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // CITA
    // ==========================================

    @OneToOne
    @JoinColumn(
            name = "cita_id",
            nullable = false,
            unique = true
    )
    private Cita cita;


    // ==========================================
    // MOTIVO DE VISITA
    // ==========================================

    @Column(
            name = "motivo_visita",
            nullable = false,
            length = 2000
    )
    private String motivoVisita;


    // ==========================================
    // HALLAZGOS CLÍNICOS
    // ==========================================

    @Column(
            name = "hallazgos_clinicos",
            columnDefinition = "TEXT"
    )
    private String hallazgosClinicos;


    // ==========================================
    // CÓDIGO CIE-10
    // ==========================================

    @Column(
            name = "codigo_cie10",
            length = 20
    )
    private String codigoCie10;


    // ==========================================
    // DIAGNÓSTICO
    // ==========================================

    @Column(
            name = "diagnostico",
            columnDefinition = "TEXT"
    )
    private String diagnostico;


    // ==========================================
    // PLAN DE TRATAMIENTO
    // ==========================================

    @Column(
            name = "plan_tratamiento",
            columnDefinition = "TEXT"
    )
    private String planTratamiento;


    // ==========================================
    // NOTAS ADICIONALES
    // ==========================================

    @Column(
            name = "notas_adicionales",
            columnDefinition = "TEXT"
    )
    private String notasAdicionales;


    // ==========================================
    // ESTADO DE CONSULTA
    // ==========================================

    @Column(
            name = "estado_consulta",
            nullable = false,
            length = 30
    )
    private String estadoConsulta = "EN_CURSO";


    // ==========================================
    // FECHA DE CREACIÓN
    // ==========================================

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    // ==========================================
    // FECHA DE FINALIZACIÓN
    // ==========================================

    @Column(
            name = "fecha_finalizacion"
    )
    private LocalDateTime fechaFinalizacion;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public ConsultaMedica() {
    }


    // ==========================================
    // PRE PERSIST
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {

            fechaCreacion =
                    LocalDateTime.now();
        }


        if (estadoConsulta == null
                || estadoConsulta.isBlank()) {

            estadoConsulta =
                    "EN_CURSO";
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


    public String getMotivoVisita() {

        return motivoVisita;
    }


    public void setMotivoVisita(
            String motivoVisita) {

        this.motivoVisita =
                motivoVisita;
    }


    public String getHallazgosClinicos() {

        return hallazgosClinicos;
    }


    public void setHallazgosClinicos(
            String hallazgosClinicos) {

        this.hallazgosClinicos =
                hallazgosClinicos;
    }


    public String getCodigoCie10() {

        return codigoCie10;
    }


    public void setCodigoCie10(
            String codigoCie10) {

        this.codigoCie10 =
                codigoCie10;
    }


    public String getDiagnostico() {

        return diagnostico;
    }


    public void setDiagnostico(
            String diagnostico) {

        this.diagnostico =
                diagnostico;
    }


    public String getPlanTratamiento() {

        return planTratamiento;
    }


    public void setPlanTratamiento(
            String planTratamiento) {

        this.planTratamiento =
                planTratamiento;
    }


    public String getNotasAdicionales() {

        return notasAdicionales;
    }


    public void setNotasAdicionales(
            String notasAdicionales) {

        this.notasAdicionales =
                notasAdicionales;
    }


    public String getEstadoConsulta() {

        return estadoConsulta;
    }


    public void setEstadoConsulta(
            String estadoConsulta) {

        this.estadoConsulta =
                estadoConsulta;
    }


    public LocalDateTime getFechaCreacion() {

        return fechaCreacion;
    }


    public void setFechaCreacion(
            LocalDateTime fechaCreacion) {

        this.fechaCreacion =
                fechaCreacion;
    }


    public LocalDateTime getFechaFinalizacion() {

        return fechaFinalizacion;
    }


    public void setFechaFinalizacion(
            LocalDateTime fechaFinalizacion) {

        this.fechaFinalizacion =
                fechaFinalizacion;
    }
}