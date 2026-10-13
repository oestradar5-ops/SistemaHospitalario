package com.hospital.his.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas_medicas")
public class RecetaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(name = "indicaciones_generales", columnDefinition = "TEXT")
    private String indicacionesGenerales;

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(
            mappedBy = "receta",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RecetaMedicamento> medicamentos = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    public RecetaMedica() {
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

    public String getIndicacionesGenerales() {
        return indicacionesGenerales;
    }

    public void setIndicacionesGenerales(String indicacionesGenerales) {
        this.indicacionesGenerales = indicacionesGenerales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<RecetaMedicamento> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<RecetaMedicamento> medicamentos) {
        this.medicamentos = medicamentos;
    }
}