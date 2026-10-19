package com.hospital.his.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sucursales")
public class Sucursal {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // NOMBRE DE LA SUCURSAL
    // ==========================================

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    // ==========================================
    // DIRECCIÓN DE LA SUCURSAL
    // ==========================================

    @Column(length = 200)
    private String direccion;

    // ==========================================
    // TELÉFONO - CU-14
    // ==========================================

    @Column(name = "telefono", length = 8)
    private String telefono;

    // ==========================================
    // ESTADO DE LA SUCURSAL
    // ==========================================

    @Column(nullable = false)
    private Boolean activo = true;

    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public Sucursal() {
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

    // ==========================================
    // NOMBRE
    // ==========================================

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // ==========================================
    // DIRECCIÓN
    // ==========================================

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    // ==========================================
    // TELÉFONO
    // ==========================================

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // ==========================================
    // ESTADO
    // ==========================================

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}