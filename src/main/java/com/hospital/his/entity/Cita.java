package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(
            name = "paciente_id",
            nullable = false
    )
    private Usuario paciente;


    @ManyToOne
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Medico medico;


    @ManyToOne
    @JoinColumn(
            name = "sucursal_id",
            nullable = false
    )
    private Sucursal sucursal;


    @ManyToOne
    @JoinColumn(
            name = "especialidad_id",
            nullable = false
    )
    private Especialidad especialidad;


    @ManyToOne
    @JoinColumn(
            name = "horario_id",
            nullable = false
    )
    private HorarioMedico horario;

    @Column(
            name = "motivo_consulta",
            nullable = false,
            length = 2000
    )
    private String motivoConsulta;


    @Column(nullable = false, length = 50)
    private String estado = "PENDIENTE_DE_PAGO";


    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
            name = "hora_llegada"
    )
    private LocalDateTime horaLlegada;


    @Column(
            name = "prioridad",
            nullable = false,
            length = 30
    )
    private String prioridad = "NORMAL";
    public Cita() {
    }
    @Column(
            name = "origen_cita",
            nullable = false,
            length = 30
    )
    private String origenCita = "PORTAL";

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion =
                    LocalDateTime.now();
        }

        if (estado == null ||
                estado.isBlank()) {

            estado =
                    "PENDIENTE_DE_PAGO";
        }

        if (prioridad == null ||
                prioridad.isBlank()) {

            prioridad =
                    "NORMAL";
        }

        if (origenCita == null ||
                origenCita.isBlank()) {

            origenCita =
                    "PORTAL";
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Usuario getPaciente() {
        return paciente;
    }

    public void setPaciente(
            Usuario paciente) {

        this.paciente = paciente;
    }


    public Medico getMedico() {
        return medico;
    }

    public void setMedico(
            Medico medico) {

        this.medico = medico;
    }


    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(
            Sucursal sucursal) {

        this.sucursal = sucursal;
    }


    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(
            Especialidad especialidad) {

        this.especialidad = especialidad;
    }


    public HorarioMedico getHorario() {
        return horario;
    }

    public void setHorario(
            HorarioMedico horario) {

        this.horario = horario;
    }


    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(
            String motivoConsulta) {

        this.motivoConsulta =
                motivoConsulta;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado) {

        this.estado = estado;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion) {

        this.fechaCreacion =
                fechaCreacion;
    }
    public LocalDateTime getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(
            LocalDateTime horaLlegada) {

        this.horaLlegada = horaLlegada;
    }


    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(
            String prioridad) {

        this.prioridad = prioridad;
    }
    public String getOrigenCita() {
        return origenCita;
    }

    public void setOrigenCita(String origenCita) {
        this.origenCita = origenCita;
    }
}