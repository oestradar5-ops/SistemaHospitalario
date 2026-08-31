package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "horarios_medicos")
public class HorarioMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Medico medico;


    @Column(nullable = false)
    private LocalDate fecha;


    @Column(nullable = false)
    private LocalTime hora;


    @Column(nullable = false)
    private Boolean disponible = true;


    public HorarioMedico() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Medico getMedico() {
        return medico;
    }

    public void setMedico(
            Medico medico) {

        this.medico = medico;
    }


    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(
            LocalDate fecha) {

        this.fecha = fecha;
    }


    public LocalTime getHora() {
        return hora;
    }

    public void setHora(
            LocalTime hora) {

        this.hora = hora;
    }


    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(
            Boolean disponible) {

        this.disponible = disponible;
    }
}