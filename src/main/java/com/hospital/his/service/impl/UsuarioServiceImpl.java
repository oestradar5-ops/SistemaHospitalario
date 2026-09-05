package com.hospital.his.service.impl;

import com.hospital.his.dto.LoginResponse;
import com.hospital.his.dto.VerificacionDpiResponse;
import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.UsuarioRepository;
import com.hospital.his.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // VERIFICAR DPI
    // ==========================================

    @Override
    public VerificacionDpiResponse verificarDpi(String dpi) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByDpi(dpi);

        if (usuarioOptional.isEmpty()) {

            return new VerificacionDpiResponse(
                    false,
                    "NO_REGISTRADO",
                    "No se encontró un registro asociado a este DPI. Será redirigido al formulario de registro."
            );
        }

        Usuario usuario = usuarioOptional.get();

        if ("PACIENTE".equalsIgnoreCase(usuario.getRol())) {

            return new VerificacionDpiResponse(
                    true,
                    "PACIENTE",
                    "Paciente registrado."
            );
        }

        return new VerificacionDpiResponse(
                true,
                "INTERNO",
                "Este DPI pertenece a un usuario del sistema interno. Por favor, contacte a recepción."
        );
    }

    // ==========================================
    // LOGIN DEL PACIENTE
    // ==========================================

    @Override
    public LoginResponse iniciarSesion(
            String nombreUsuario,
            String password) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOptional.isEmpty()) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }

        Usuario usuario = usuarioOptional.get();

        // Verificar bloqueo
        LoginResponse bloqueo = verificarBloqueo(usuario);

        if (bloqueo != null) {
            return bloqueo;
        }

        // Contraseña incorrecta
        if (!usuario.getPassword().equals(password)) {
            return registrarIntentoFallido(usuario);
        }

        // Cuenta inactiva
        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            return new LoginResponse(
                    "ERROR",
                    "La cuenta se encuentra inactiva.",
                    null
            );
        }

        // Verificar rol
        if (!"PACIENTE".equalsIgnoreCase(usuario.getRol())) {

            return new LoginResponse(
                    "ROL_NO_AUTORIZADO",
                    "Este acceso es exclusivo para pacientes. Si es personal del hospital, use el panel administrativo.",
                    usuario.getRol()
            );
        }

        // Login correcto
        reiniciarIntentos(usuario);

        return new LoginResponse(
                "OK",
                "Inicio de sesión exitoso.",
                usuario.getRol(),
                null,
                usuario.getNombreUsuario()
        );
    }

    // ==========================================
    // LOGIN DEL ADMINISTRADOR
    // ==========================================

    @Override
    public LoginResponse iniciarSesionAdmin(
            String nombreUsuario,
            String password) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOptional.isEmpty()) {

            return new LoginResponse(
                    "ERROR",
                    "Usuario o contraseña incorrectos.",
                    null
            );
        }

        Usuario usuario = usuarioOptional.get();

        // Verificar bloqueo
        LoginResponse bloqueo = verificarBloqueo(usuario);

        if (bloqueo != null) {
            return bloqueo;
        }

        // Contraseña incorrecta
        if (!usuario.getPassword().equals(password)) {
            return registrarIntentoFallido(usuario);
        }

        // Cuenta inactiva
        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            return new LoginResponse(
                    "ERROR",
                    "La cuenta se encuentra inactiva.",
                    null
            );
        }

        // Verificar rol
        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {

            return new LoginResponse(
                    "ROL_NO_AUTORIZADO",
                    "Este acceso es exclusivo para administradores.",
                    usuario.getRol()
            );
        }

        // Login correcto
        reiniciarIntentos(usuario);

        return new LoginResponse(
                "OK",
                "Inicio de sesión administrativo exitoso.",
                usuario.getRol(),
                null,
                usuario.getNombreUsuario()
        );
    }

    // ==========================================
    // REGISTRAR INTENTO FALLIDO
    // ==========================================

    private LoginResponse registrarIntentoFallido(Usuario usuario) {

        int intentosActuales =
                usuario.getIntentosFallidos() == null
                        ? 0
                        : usuario.getIntentosFallidos();

        int nuevosIntentos = intentosActuales + 1;

        usuario.setIntentosFallidos(nuevosIntentos);

        // Si llegó a 5 intentos
        if (nuevosIntentos >= MAX_INTENTOS) {

            LocalDateTime bloqueadoHasta =
                    LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);

            usuario.setBloqueadoHasta(bloqueadoHasta);

            usuarioRepository.save(usuario);

            return new LoginResponse(
                    "BLOQUEADO",
                    "Ha alcanzado el máximo de intentos. Su cuenta ha sido bloqueada temporalmente durante 15 minutos.",
                    usuario.getRol(),
                    (long) MINUTOS_BLOQUEO * 60,
                    usuario.getNombreUsuario()
            );
        }

        usuarioRepository.save(usuario);

        int intentosRestantes =
                MAX_INTENTOS - nuevosIntentos;

        return new LoginResponse(
                "ERROR",
                "Usuario o contraseña incorrectos. Intentos restantes: "
                        + intentosRestantes + ".",
                null
        );
    }

    // ==========================================
    // VERIFICAR BLOQUEO
    // ==========================================

    private LoginResponse verificarBloqueo(Usuario usuario) {

        if (usuario.getBloqueadoHasta() == null) {
            return null;
        }

        LocalDateTime ahora = LocalDateTime.now();

        // Todavía sigue bloqueado
        if (usuario.getBloqueadoHasta().isAfter(ahora)) {

            long segundos =
                    Duration.between(
                            ahora,
                            usuario.getBloqueadoHasta()
                    ).getSeconds();

            long minutosRestantes =
                    (segundos + 59) / 60;

            return new LoginResponse(
                    "BLOQUEADO",
                    "Cuenta bloqueada temporalmente. Intente nuevamente en aproximadamente "
                            + minutosRestantes + " minuto(s).",
                    usuario.getRol(),
                    segundos,
                    usuario.getNombreUsuario()
            );
        }

        // Ya pasaron los 15 minutos
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);

        return null;
    }

    // ==========================================
    // REINICIAR INTENTOS
    // ==========================================

    private void reiniciarIntentos(Usuario usuario) {

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);
    }
}