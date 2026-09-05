package com.hospital.his.controller;

import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class MedicoLoginController {

    private final UsuarioRepository usuarioRepository;

    public MedicoLoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // MOSTRAR LOGIN DEL MÉDICO
    // ==========================================

    @GetMapping("/medico/login")
    public String mostrarLogin(HttpSession session) {

        String rol = (String) session.getAttribute("rol");

        if ("MEDICO".equalsIgnoreCase(rol)) {
            return "redirect:/medico";
        }

        return "medico/login";
    }


    // ==========================================
    // INICIAR SESIÓN
    // ==========================================

    @PostMapping("/medico/login")
    public String iniciarSesion(
            @RequestParam String nombreUsuario,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOptional.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "medico/login";
        }

        Usuario usuario = usuarioOptional.get();

        // Verificar contraseña
        if (!usuario.getPassword().equals(password)) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "medico/login";
        }

        // Verificar que esté activo
        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            model.addAttribute(
                    "error",
                    "La cuenta se encuentra inactiva."
            );

            return "medico/login";
        }

        // Verificar rol
        if (!"MEDICO".equalsIgnoreCase(usuario.getRol())) {

            model.addAttribute(
                    "error",
                    "Este acceso es exclusivo para médicos."
            );

            return "medico/login";
        }

        // Crear sesión
        session.setAttribute(
                "usuario",
                usuario.getNombreUsuario()
        );

        session.setAttribute(
                "rol",
                usuario.getRol()
        );

        return "redirect:/medico";
    }


    // ==========================================
    // CERRAR SESIÓN
    // ==========================================

    @GetMapping("/medico/logout")
    public String cerrarSesion(HttpSession session) {

        session.invalidate();

        return "redirect:/medico/login";
    }
}