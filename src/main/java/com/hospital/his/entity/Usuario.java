package com.hospital.his.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DATOS GENERALES

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 80)
    private String nombreUsuario;

    @Column(nullable = false)
    private String password;

    // DOCUMENTOS Y CONTACTO

    @Column(unique = true, length = 13)
    private String dpi;

    @Column(name = "numero_telefono", length = 20)
    private String numeroTelefono;

    @Column(length = 20)
    private String nit;

    @Column(name = "numero_seguro", length = 50)
    private String numeroSeguro;

    // DATOS DEL SISTEMA

    @Column(nullable = false, length = 50)
    private String rol;

    @Column(length = 100)
    private String sucursal;

    @Column(length = 100)
    private String especialidad;

    @Column(nullable = false)
    private Boolean activo = true;

    // SEGURIDAD DEL LOGIN

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;
}