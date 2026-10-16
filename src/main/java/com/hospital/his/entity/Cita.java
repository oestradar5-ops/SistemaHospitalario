package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // PACIENTE
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "paciente_id",
            nullable = false
    )
    private Usuario paciente;


    // ==========================================
    // MÉDICO
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Medico medico;


    // ==========================================
    // SUCURSAL
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "sucursal_id",
            nullable = false
    )
    private Sucursal sucursal;


    // ==========================================
    // ESPECIALIDAD
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "especialidad_id",
            nullable = false
    )
    private Especialidad especialidad;


    // ==========================================
    // HORARIO MÉDICO
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "horario_id",
            nullable = false
    )
    private HorarioMedico horario;


    // ==========================================
    // MOTIVO DE CONSULTA
    // ==========================================

    @Column(
            name = "motivo_consulta",
            nullable = false,
            length = 2000
    )
    private String motivoConsulta;


    // ==========================================
    // ESTADO DE LA CITA
    // ==========================================

    @Column(
            nullable = false,
            length = 50
    )
    private String estado = "PENDIENTE_DE_PAGO";


    // ==========================================
    // FECHA DE CREACIÓN
    // ==========================================

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    // ==========================================
    // HORA DE LLEGADA
    // ==========================================

    @Column(
            name = "hora_llegada"
    )
    private LocalDateTime horaLlegada;


    // ==========================================
    // PRIORIDAD
    // ==========================================

    @Column(
            name = "prioridad",
            nullable = false,
            length = 30
    )
    private String prioridad = "NORMAL";


    // ==========================================
    // ORIGEN DE LA CITA
    // ==========================================

    @Column(
            name = "origen_cita",
            nullable = false,
            length = 30
    )
    private String origenCita = "PORTAL";


    // ==========================================
    // CU-12 - CONSULTA MÉDICA DE ORIGEN
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "consulta_origen_id"
    )
    private ConsultaMedica consultaOrigen;


    // ==========================================
    // CU-12 - TIPO DE SEGUIMIENTO
    // ==========================================

    @Column(
            name = "tipo_seguimiento",
            length = 50
    )
    private String tipoSeguimiento;


    // ==========================================
    // CU-12 - CONTROL DE RECORDATORIOS
    // ==========================================

    @Column(
            name = "recordatorio_seguimiento_enviado"
    )
    private Boolean recordatorioSeguimientoEnviado = false;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public Cita() {
    }


    // ==========================================
    // VALORES PREDETERMINADOS
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {

            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null || estado.isBlank()) {

            estado = "PENDIENTE_DE_PAGO";
        }

        if (prioridad == null || prioridad.isBlank()) {

            prioridad = "NORMAL";
        }

        if (origenCita == null || origenCita.isBlank()) {

            origenCita = "PORTAL";
        }

        if (recordatorioSeguimientoEnviado == null) {

            recordatorioSeguimientoEnviado = false;
        }
    }


    // ==========================================
    // GETTERS Y SETTERS GENERALES
    // ==========================================

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }


    // ==========================================
    // PACIENTE
    // ==========================================

    public Usuario getPaciente() {

        return paciente;
    }

    public void setPaciente(Usuario paciente) {

        this.paciente = paciente;
    }


    // ==========================================
    // MÉDICO
    // ==========================================

    public Medico getMedico() {

        return medico;
    }

    public void setMedico(Medico medico) {

        this.medico = medico;
    }


    // ==========================================
    // SUCURSAL
    // ==========================================

    public Sucursal getSucursal() {

        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {

        this.sucursal = sucursal;
    }


    // ==========================================
    // ESPECIALIDAD
    // ==========================================

    public Especialidad getEspecialidad() {

        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {

        this.especialidad = especialidad;
    }


    // ==========================================
    // HORARIO MÉDICO
    // ==========================================

    public HorarioMedico getHorario() {

        return horario;
    }

    public void setHorario(HorarioMedico horario) {

        this.horario = horario;
    }


    // ==========================================
    // MOTIVO DE CONSULTA
    // ==========================================

    public String getMotivoConsulta() {

        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {

        this.motivoConsulta = motivoConsulta;
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
    // HORA DE LLEGADA
    // ==========================================

    public LocalDateTime getHoraLlegada() {

        return horaLlegada;
    }

    public void setHoraLlegada(LocalDateTime horaLlegada) {

        this.horaLlegada = horaLlegada;
    }


    // ==========================================
    // PRIORIDAD
    // ==========================================

    public String getPrioridad() {

        return prioridad;
    }

    public void setPrioridad(String prioridad) {

        this.prioridad = prioridad;
    }


    // ==========================================
    // ORIGEN DE CITA
    // ==========================================

    public String getOrigenCita() {

        return origenCita;
    }

    public void setOrigenCita(String origenCita) {

        this.origenCita = origenCita;
    }


    // ==========================================
    // CU-12 - CONSULTA ORIGINAL
    // ==========================================

    public ConsultaMedica getConsultaOrigen() {

        return consultaOrigen;
    }

    public void setConsultaOrigen(
            ConsultaMedica consultaOrigen) {

        this.consultaOrigen = consultaOrigen;
    }


    // ==========================================
    // CU-12 - TIPO DE SEGUIMIENTO
    // ==========================================

    public String getTipoSeguimiento() {

        return tipoSeguimiento;
    }

    public void setTipoSeguimiento(
            String tipoSeguimiento) {

        this.tipoSeguimiento = tipoSeguimiento;
    }


    // ==========================================
    // CU-12 - RECORDATORIO ENVIADO
    // ==========================================

    public Boolean getRecordatorioSeguimientoEnviado() {

        return recordatorioSeguimientoEnviado;
    }

    public void setRecordatorioSeguimientoEnviado(
            Boolean recordatorioSeguimientoEnviado) {

        this.recordatorioSeguimientoEnviado =
                recordatorioSeguimientoEnviado;
    }

}