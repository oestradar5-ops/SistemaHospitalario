package com.hospital.his.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalogo_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // CÓDIGO DEL ROL
    // ==========================================

    @Column(
            name = "codigo",
            nullable = false,
            unique = true,
            length = 50
    )
    private String codigo;

    // ==========================================
    // NOMBRE DEL ROL
    // ==========================================

    @Column(
            name = "nombre",
            nullable = false,
            length = 100
    )
    private String nombre;

    // ==========================================
    // DESCRIPCIÓN
    // ==========================================

    @Column(
            name = "descripcion",
            columnDefinition = "TEXT"
    )
    private String descripcion;

    // ==========================================
    // ESTADO DEL ROL
    // ==========================================

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ==========================================
    // ROL PROTEGIDO
    // ==========================================

    @Builder.Default
    @Column(name = "protegido", nullable = false)
    private Boolean protegido = false;
}