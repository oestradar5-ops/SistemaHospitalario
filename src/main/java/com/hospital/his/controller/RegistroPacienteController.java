package com.hospital.his.controller;

import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.UsuarioRepository;
import com.hospital.his.service.CorreoService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
public class RegistroPacienteController {

    private final UsuarioRepository usuarioRepository;
    private final CorreoService correoService;


    public RegistroPacienteController(
            UsuarioRepository usuarioRepository,
            CorreoService correoService) {

        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
    }


    // ==========================================
    // MOSTRAR FORMULARIO
    // ==========================================

    @GetMapping("/registro")
    public String mostrarRegistro() {

        return "paciente/registro";
    }


    // ==========================================
    // REGISTRAR PACIENTE
    // ==========================================

    @ResponseBody
    @PostMapping("/api/registro")
    public Map<String, Object> registrarPaciente(
            @RequestBody Usuario usuario) {


        // NOMBRE

        if (
                usuario.getNombreCompleto() == null ||
                        usuario.getNombreCompleto().isBlank()
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre completo es obligatorio."
            );
        }


        // DPI

        if (
                usuario.getDpi() == null ||
                        !usuario.getDpi().matches("\\d{13}")
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El DPI debe contener exactamente 13 dígitos."
            );
        }


        // DPI DUPLICADO

        if (
                usuarioRepository.existsByDpi(
                        usuario.getDpi()
                )
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Ya existe una cuenta registrada con este número de DPI. " +
                            "Si ya tiene cuenta, inicie sesión."
            );
        }


        // NIT

        if (
                usuario.getNit() == null ||
                        usuario.getNit().isBlank()
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El NIT es obligatorio."
            );
        }


        // TELÉFONO

        if (
                usuario.getNumeroTelefono() == null ||
                        usuario.getNumeroTelefono().isBlank()
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El número de teléfono es obligatorio."
            );
        }


        // CORREO

        if (
                usuario.getCorreoElectronico() == null ||
                        usuario.getCorreoElectronico().isBlank()
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El correo electrónico es obligatorio."
            );
        }


        // CORREO DUPLICADO

        if (
                usuarioRepository
                        .existsByCorreoElectronicoIgnoreCase(
                                usuario.getCorreoElectronico()
                        )
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Ya existe una cuenta registrada con este correo electrónico."
            );
        }


        // NOMBRE DE USUARIO

        if (
                usuario.getNombreUsuario() == null ||
                        usuario.getNombreUsuario().length() < 8 ||
                        usuario.getNombreUsuario().length() > 9
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre de usuario debe tener entre 8 y 9 caracteres."
            );
        }


        if (
                usuarioRepository
                        .existsByNombreUsuarioIgnoreCase(
                                usuario.getNombreUsuario()
                        )
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre de usuario ya está registrado."
            );
        }


        // CONTRASEÑA

        if (
                usuario.getPassword() == null ||
                        usuario.getPassword().length() < 12
        ) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La contraseña debe contener como mínimo 12 caracteres."
            );
        }


        // ==========================================
        // DATOS AUTOMÁTICOS DEL PACIENTE
        // ==========================================

        usuario.setRol("PACIENTE");
        usuario.setActivo(true);
        usuario.setSucursal(null);
        usuario.setEspecialidad(null);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);


        try {

            // Guardar paciente
            Usuario usuarioGuardado =
                    usuarioRepository.save(usuario);


            // Enviar correo de bienvenida
            correoService.enviarBienvenida(
                    usuarioGuardado.getCorreoElectronico(),
                    usuarioGuardado.getNombreCompleto()
            );


            return Map.of(
                    "estado", "OK",
                    "mensaje",
                    "¡Registro exitoso! Su cuenta ha sido creada. " +
                            "Ahora puede iniciar sesión con sus credenciales."
            );


        } catch (DataIntegrityViolationException e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Uno de los datos ingresados ya se encuentra registrado."
            );


        } catch (Exception e) {

            e.printStackTrace();

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El usuario fue procesado, pero ocurrió un problema al completar el registro o enviar el correo."
            );
        }
    }
}