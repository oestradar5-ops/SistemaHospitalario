package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Controller
public class AdminUsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaUsuarioRepository auditoriaUsuarioRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminUsuarioController(
            UsuarioRepository usuarioRepository,
            AuditoriaUsuarioRepository auditoriaUsuarioRepository) {

        this.usuarioRepository = usuarioRepository;
        this.auditoriaUsuarioRepository = auditoriaUsuarioRepository;
    }


    // =====================================================
    // COMPROBAR SI EL USUARIO ES ADMINISTRADOR
    // =====================================================

    private boolean esAdministrador(HttpSession session) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        return usuario != null
                && "ADMIN".equalsIgnoreCase(rol);
    }


    // =====================================================
    // REGISTRAR AUDITORÍA
    // =====================================================

    private void registrarAuditoria(
            HttpSession session,
            String accion,
            String usuarioAfectado,
            String descripcion) {

        String administrador =
                (String) session.getAttribute("usuario");

        AuditoriaUsuario auditoria =
                new AuditoriaUsuario(
                        administrador,
                        accion,
                        usuarioAfectado,
                        descripcion
                );

        auditoriaUsuarioRepository.save(auditoria);
    }


    // =====================================================
    // LISTAR Y BUSCAR USUARIOS
    // =====================================================

    @GetMapping("/admin/usuarios")
    public String listarUsuarios(

            @RequestParam(defaultValue = "0")
            int pagina,

            @RequestParam(defaultValue = "10")
            int tamanio,

            @RequestParam(defaultValue = "")
            String campo,

            @RequestParam(defaultValue = "")
            String buscar,

            HttpSession session,

            Model model) {


        // Verificar sesión administrativa

        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        String usuarioActual =
                (String) session.getAttribute("usuario");


        // Tamaños permitidos

        if (tamanio != 10 &&
                tamanio != 25 &&
                tamanio != 50) {

            tamanio = 10;
        }


        // Evitar páginas negativas

        if (pagina < 0) {

            pagina = 0;
        }


        Pageable pageable =
                PageRequest.of(
                        pagina,
                        tamanio
                );


        Page<Usuario> paginaUsuarios;


        // =================================================
        // SIN BÚSQUEDA
        // =================================================

        if (buscar == null ||
                buscar.isBlank()) {

            paginaUsuarios =
                    usuarioRepository.findAll(
                            pageable
                    );

        } else {

            buscar =
                    buscar.trim();


            // =============================================
            // BUSCAR SEGÚN EL CAMPO
            // =============================================

            switch (campo) {

                case "id":

                    try {

                        Long id =
                                Long.parseLong(buscar);


                        Usuario encontrado =
                                usuarioRepository
                                        .findById(id)
                                        .orElse(null);


                        if (encontrado == null) {

                            paginaUsuarios =
                                    Page.empty(
                                            pageable
                                    );

                        } else {

                            paginaUsuarios =
                                    new PageImpl<>(
                                            List.of(encontrado),
                                            pageable,
                                            1
                                    );
                        }

                    } catch (NumberFormatException e) {

                        paginaUsuarios =
                                Page.empty(
                                        pageable
                                );
                    }

                    break;


                case "nombre":

                    paginaUsuarios =
                            usuarioRepository
                                    .findByNombreCompletoContainingIgnoreCase(
                                            buscar,
                                            pageable
                                    );

                    break;


                case "correo":

                    paginaUsuarios =
                            usuarioRepository
                                    .findByCorreoElectronicoContainingIgnoreCase(
                                            buscar,
                                            pageable
                                    );

                    break;


                case "rol":

                    paginaUsuarios =
                            usuarioRepository
                                    .findByRolContainingIgnoreCase(
                                            buscar,
                                            pageable
                                    );

                    break;


                case "usuario":

                    paginaUsuarios =
                            usuarioRepository
                                    .findByNombreUsuarioContainingIgnoreCase(
                                            buscar,
                                            pageable
                                    );

                    break;


                case "dpi":

                    paginaUsuarios =
                            usuarioRepository
                                    .findByDpiContaining(
                                            buscar,
                                            pageable
                                    );

                    break;


                default:

                    paginaUsuarios =
                            usuarioRepository.findAll(
                                    pageable
                            );
            }
        }


        // =================================================
        // DATOS PARA THYMELEAF
        // =================================================

        model.addAttribute(
                "usuarios",
                paginaUsuarios
        );

        model.addAttribute(
                "campo",
                campo
        );

        model.addAttribute(
                "buscar",
                buscar
        );

        model.addAttribute(
                "tamanio",
                tamanio
        );

        model.addAttribute(
                "usuarioActual",
                usuarioActual
        );


        return "admin/usuarios/listar";
    }


    // =====================================================
    // MOSTRAR FORMULARIO CREAR USUARIO
    // =====================================================

    @GetMapping("/admin/usuarios/crear")
    public String mostrarFormularioCrear(
            HttpSession session) {


        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        return "admin/usuarios/crear";
    }


    // =====================================================
    // CREAR USUARIO
    // =====================================================

    @ResponseBody
    @PostMapping("/api/admin/usuarios")
    public Map<String, Object> crearUsuario(

            @RequestBody Usuario usuario,

            HttpSession session) {


        // Verificar administrador

        if (!esAdministrador(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        // =================================================
        // VALIDAR NOMBRE
        // =================================================

        if (usuario.getNombreCompleto() == null ||
                usuario.getNombreCompleto().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre completo es obligatorio."
            );
        }


        // =================================================
        // VALIDAR CORREO
        // =================================================

        if (usuario.getCorreoElectronico() == null ||
                usuario.getCorreoElectronico().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El correo electrónico es obligatorio."
            );
        }


        // =================================================
        // VALIDAR NOMBRE DE USUARIO
        // =================================================

        if (usuario.getNombreUsuario() == null ||
                usuario.getNombreUsuario().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre de usuario es obligatorio."
            );
        }


        // =================================================
        // VALIDAR CONTRASEÑA
        // =================================================

        if (usuario.getPassword() == null ||
                usuario.getPassword().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La contraseña es obligatoria."
            );
        }


        // =================================================
        // VALIDAR ROL
        // =================================================

        if (usuario.getRol() == null ||
                usuario.getRol().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Debe seleccionar un rol."
            );
        }


        // =================================================
        // VALIDAR DPI
        // =================================================

        if (usuario.getDpi() != null &&
                !usuario.getDpi().isBlank()) {

            if (!usuario.getDpi()
                    .matches("\\d{13}")) {

                return Map.of(
                        "estado", "ERROR",
                        "mensaje",
                        "El DPI debe contener exactamente 13 dígitos."
                );
            }
        }


        // =================================================
        // ESPECIALIDAD SOLO PARA MÉDICOS
        // =================================================

        if (!"MEDICO".equalsIgnoreCase(
                usuario.getRol())) {

            usuario.setEspecialidad(null);
        }


        // =================================================
        // VALORES INICIALES
        // =================================================

        if (usuario.getActivo() == null) {

            usuario.setActivo(true);
        }


        usuario.setIntentosFallidos(0);

        usuario.setBloqueadoHasta(null);


        // =================================================
        // GUARDAR USUARIO
        // =================================================

        try {

            Usuario usuarioGuardado =
                    usuarioRepository.save(usuario);


            // Registrar auditoría

            registrarAuditoria(
                    session,
                    "CREAR",
                    usuarioGuardado.getNombreUsuario(),
                    "Usuario creado correctamente."
            );


            return Map.of(
                    "estado", "OK",
                    "mensaje",
                    "Usuario creado correctamente."
            );


        } catch (DataIntegrityViolationException e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El correo electrónico, nombre de usuario o DPI ya se encuentra registrado."
            );


        } catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No fue posible crear el usuario."
            );
        }
    }


    // =====================================================
    // MOSTRAR FORMULARIO EDITAR USUARIO
    // =====================================================

    @GetMapping("/admin/usuarios/editar/{id}")
    public String mostrarFormularioEditar(

            @PathVariable Long id,

            HttpSession session,

            Model model) {


        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);


        if (usuario == null) {

            return "redirect:/admin/usuarios";
        }


        model.addAttribute(
                "usuario",
                usuario
        );


        return "admin/usuarios/editar";
    }


    // =====================================================
    // ACTUALIZAR USUARIO
    // =====================================================

    @ResponseBody
    @PutMapping("/api/admin/usuarios/{id}")
    public Map<String, Object> actualizarUsuario(

            @PathVariable Long id,

            @RequestBody Usuario datos,

            HttpSession session) {


        // Verificar administrador

        if (!esAdministrador(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        // Buscar usuario

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);


        if (usuario == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Usuario no encontrado."
            );
        }


        // =================================================
        // VALIDAR NOMBRE
        // =================================================

        if (datos.getNombreCompleto() == null ||
                datos.getNombreCompleto().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre completo es obligatorio."
            );
        }


        // =================================================
        // VALIDAR CORREO
        // =================================================

        if (datos.getCorreoElectronico() == null ||
                datos.getCorreoElectronico().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El correo electrónico es obligatorio."
            );
        }


        // =================================================
        // VALIDAR USUARIO
        // =================================================

        if (datos.getNombreUsuario() == null ||
                datos.getNombreUsuario().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El nombre de usuario es obligatorio."
            );
        }


        // =================================================
        // VALIDAR ROL
        // =================================================

        if (datos.getRol() == null ||
                datos.getRol().isBlank()) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Debe seleccionar un rol."
            );
        }


        // =================================================
        // VALIDAR DPI
        // =================================================

        if (datos.getDpi() != null &&
                !datos.getDpi().isBlank() &&
                !datos.getDpi()
                        .matches("\\d{13}")) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El DPI debe contener exactamente 13 dígitos."
            );
        }


        // =================================================
        // ACTUALIZAR DATOS
        // =================================================

        usuario.setNombreCompleto(
                datos.getNombreCompleto()
        );

        usuario.setCorreoElectronico(
                datos.getCorreoElectronico()
        );

        usuario.setNombreUsuario(
                datos.getNombreUsuario()
        );

        usuario.setDpi(
                datos.getDpi()
        );

        usuario.setNumeroTelefono(
                datos.getNumeroTelefono()
        );

        usuario.setRol(
                datos.getRol()
        );

        usuario.setNit(
                datos.getNit()
        );

        usuario.setNumeroSeguro(
                datos.getNumeroSeguro()
        );

        usuario.setSucursal(
                datos.getSucursal()
        );

        usuario.setActivo(
                datos.getActivo()
        );


        // =================================================
        // ESPECIALIDAD SOLO PARA MÉDICOS
        // =================================================

        if ("MEDICO".equalsIgnoreCase(
                datos.getRol())) {

            usuario.setEspecialidad(
                    datos.getEspecialidad()
            );

        } else {

            usuario.setEspecialidad(null);
        }


        // =================================================
        // CONTRASEÑA OPCIONAL
        // =================================================

        if (datos.getPassword() != null &&
                !datos.getPassword().isBlank()) {

            usuario.setPassword(
                    datos.getPassword()
            );
        }


        // =================================================
        // GUARDAR CAMBIOS
        // =================================================

        try {

            Usuario usuarioActualizado =
                    usuarioRepository.save(usuario);


            // Registrar auditoría

            registrarAuditoria(
                    session,
                    "EDITAR",
                    usuarioActualizado.getNombreUsuario(),
                    "Usuario actualizado correctamente."
            );


            return Map.of(
                    "estado", "OK",
                    "mensaje",
                    "Usuario actualizado correctamente."
            );


        } catch (DataIntegrityViolationException e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El correo electrónico, nombre de usuario o DPI ya se encuentra registrado."
            );


        } catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No fue posible actualizar el usuario."
            );
        }
    }


    // =====================================================
    // ELIMINAR USUARIO
    // =====================================================

    @ResponseBody
    @DeleteMapping("/api/admin/usuarios/{id}")
    public Map<String, Object> eliminarUsuario(

            @PathVariable Long id,

            HttpSession session) {


        // Verificar administrador

        if (!esAdministrador(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        // Buscar usuario

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);


        if (usuario == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El usuario no existe."
            );
        }


        // =================================================
        // EVITAR ELIMINAR SU PROPIA CUENTA
        // =================================================

        String usuarioActual =
                (String) session.getAttribute("usuario");


        if (usuario.getNombreUsuario()
                .equalsIgnoreCase(usuarioActual)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No puede eliminar su propia cuenta mientras tiene la sesión iniciada."
            );
        }


        // =================================================
        // ELIMINAR
        // =================================================

        try {

            String nombreUsuarioEliminado =
                    usuario.getNombreUsuario();


            usuarioRepository.delete(usuario);


            // Registrar auditoría

            registrarAuditoria(
                    session,
                    "ELIMINAR",
                    nombreUsuarioEliminado,
                    "Usuario eliminado correctamente."
            );


            return Map.of(
                    "estado", "OK",
                    "mensaje",
                    "Usuario eliminado correctamente."
            );


        } catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No fue posible eliminar el usuario."
            );
        }
    }
}