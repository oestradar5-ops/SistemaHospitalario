package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "sucursal_especialidad",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sucursal_especialidad",
                        columnNames = {
                                "sucursal_id",
                                "especialidad_id"
                        }
                )
        }
)
public class SucursalEspecialidad {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


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
    // ESTADO DE LA ASIGNACIÓN
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public SucursalEspecialidad() {
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


    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}