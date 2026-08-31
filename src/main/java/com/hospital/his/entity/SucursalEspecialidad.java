package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sucursal_especialidad")
public class SucursalEspecialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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


    public SucursalEspecialidad() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }


    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(
            Especialidad especialidad) {

        this.especialidad = especialidad;
    }
}