package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medicos")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(
            name = "usuario_id",
            nullable = false
    )
    private Usuario usuario;


    @ManyToOne
    @JoinColumn(
            name = "especialidad_id",
            nullable = false
    )
    private Especialidad especialidad;


    @ManyToOne
    @JoinColumn(
            name = "sucursal_id",
            nullable = false
    )
    private Sucursal sucursal;


    @Column(nullable = false)
    private Boolean activo = true;


    public Medico() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }


    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(
            Especialidad especialidad) {

        this.especialidad = especialidad;
    }


    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(
            Sucursal sucursal) {

        this.sucursal = sucursal;
    }


    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}