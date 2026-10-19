package com.hospital.his.controller;

import com.hospital.his.entity.AuditoriaUsuario;
import com.hospital.his.entity.Rol;

import com.hospital.his.repository.AuditoriaUsuarioRepository;
import com.hospital.his.repository.RolRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/catalogos/roles")
public class CatalogoRolController {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final RolRepository rolRepository;

    private final UsuarioRepository usuarioRepository;

    private final AuditoriaUsuarioRepository auditoriaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoRolController(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaUsuarioRepository auditoriaRepository) {

        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private boolean esAdministrador(HttpSession session) {

        Object usuario = session.getAttribute("usuario");
        Object rol = session.getAttribute("rol");

        return usuario instanceof String
                && !((String) usuario).isBlank()
                && rol instanceof String
                && (
                "ADMIN".equalsIgnoreCase((String) rol)
                        || "ADMINISTRADOR".equalsIgnoreCase((String) rol)
        );
    }


    // ==========================================
    // REGISTRAR AUDITORÍA
    // ==========================================

    private void registrarAuditoria(
            HttpSession session,
            String accion,
            String descripcion) {

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                (String) session.getAttribute("usuario"),
                accion,
                null,
                descripcion
        );

        auditoriaRepository.save(auditoria);
    }

    // ==========================================
// CU-14: LISTAR ROLES DEL SISTEMA
// ==========================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listarRoles(
            HttpSession session,
            Model model) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // CONSULTAR TODOS LOS ROLES
        // ======================================

        List<Rol> roles =
                rolRepository.findAllByOrderByCodigoAsc();

        // ======================================
        // CALCULAR ESTADÍSTICAS
        // ======================================

        long totalRoles = roles.size();

        long rolesActivos = roles.stream()
                .filter(rol ->
                        Boolean.TRUE.equals(rol.getActivo()))
                .count();

        long rolesInactivos = roles.stream()
                .filter(rol ->
                        !Boolean.TRUE.equals(rol.getActivo()))
                .count();

        long rolesProtegidos = roles.stream()
                .filter(rol ->
                        Boolean.TRUE.equals(rol.getProtegido()))
                .count();

        // ======================================
        // ENVIAR DATOS A THYMELEAF
        // ======================================

        model.addAttribute(
                "roles",
                roles
        );

        model.addAttribute(
                "totalRoles",
                totalRoles
        );

        model.addAttribute(
                "rolesActivos",
                rolesActivos
        );

        model.addAttribute(
                "rolesInactivos",
                rolesInactivos
        );

        model.addAttribute(
                "rolesProtegidos",
                rolesProtegidos
        );

        // ======================================
        // MOSTRAR PANTALLA ADMINISTRATIVA
        // ======================================

        return "admin/catalogos/roles/index";
    }

    // ==========================================
// CU-14: FORMULARIO DE REGISTRO DE ROLES
// ==========================================

    @GetMapping("/create")
    public String mostrarRegistro(
            HttpSession session,
            Model model) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // PREPARAR NUEVO ROL
        // ======================================

        Rol rol = new Rol();

        rol.setCodigo("");
        rol.setNombre("");
        rol.setDescripcion("");
        rol.setActivo(true);
        rol.setProtegido(false);

        // ======================================
        // ENVIAR DATOS AL FORMULARIO
        // ======================================

        model.addAttribute(
                "rol",
                rol
        );

        model.addAttribute(
                "modoEdicion",
                false
        );

        // ======================================
        // MOSTRAR FORMULARIO
        // ======================================

        return "admin/catalogos/roles/form";
    }

    // ==========================================
// CU-14: REGISTRAR NUEVO ROL
// ==========================================

    @PostMapping("/create")
    @Transactional
    public String registrarRol(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaFormulario =
                "redirect:/admin/catalogos/roles/create";

        String rutaListado =
                "redirect:/admin/catalogos/roles";

        // ======================================
        // NORMALIZAR CAMPOS
        // ======================================

        codigo = codigo == null
                ? ""
                : codigo.trim().toUpperCase(java.util.Locale.ROOT);

        nombre = nombre == null
                ? ""
                : nombre.trim();

        descripcion = descripcion == null
                ? ""
                : descripcion.trim();

        // ======================================
        // VALIDAR CAMPOS OBLIGATORIOS
        // ======================================

        if (codigo.isBlank() || nombre.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El código y el nombre del rol son obligatorios."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR LONGITUD
        // ======================================

        if (codigo.length() > 50) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El código del rol no puede superar los 50 caracteres."
            );

            return rutaFormulario;
        }

        if (nombre.length() > 100) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre del rol no puede superar los 100 caracteres."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR FORMATO DEL CÓDIGO
        // ======================================

        if (!codigo.matches("[A-Z][A-Z0-9_]*")) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El código debe comenzar con una letra "
                            + "y contener únicamente letras mayúsculas, "
                            + "números o guiones bajos."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR CÓDIGO DUPLICADO
        // ======================================

        if (rolRepository.existsByCodigoIgnoreCase(codigo)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe un rol registrado con ese código."
            );

            return rutaFormulario;
        }

        // ======================================
        // CREAR NUEVO ROL
        // ======================================

        Rol nuevoRol = new Rol();

        nuevoRol.setCodigo(codigo);
        nuevoRol.setNombre(nombre);
        nuevoRol.setDescripcion(descripcion);
        nuevoRol.setActivo(true);
        nuevoRol.setProtegido(false);

        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        rolRepository.save(nuevoRol);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        registrarAuditoria(
                session,
                "CREAR_ROL",
                "Registró un nuevo rol"
                        + " | Código: " + codigo
                        + " | Nombre: " + nombre
                        + " | Activo: true"
                        + " | Protegido: false"
        );

        // ======================================
        // CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Rol registrado correctamente."
        );

        return rutaListado;
    }

    // ==========================================
// CU-14: MOSTRAR FORMULARIO DE EDICIÓN
// ==========================================

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String mostrarEdicion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        // ======================================
        // BUSCAR ROL POR ID
        // ======================================

        Rol rol = rolRepository.findById(id)
                .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (rol == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El rol solicitado no existe."
            );

            return "redirect:/admin/catalogos/roles";
        }

        // ======================================
        // IDENTIFICAR ROL PROTEGIDO
        // ======================================

        boolean rolProtegido =
                Boolean.TRUE.equals(rol.getProtegido());

        // ======================================
        // ENVIAR INFORMACIÓN AL FORMULARIO
        // ======================================

        model.addAttribute(
                "rol",
                rol
        );

        model.addAttribute(
                "modoEdicion",
                true
        );

        model.addAttribute(
                "rolProtegido",
                rolProtegido
        );

        // ======================================
        // MOSTRAR FORMULARIO
        // ======================================

        return "admin/catalogos/roles/form";
    }

    // ==========================================
// CU-14: ACTUALIZAR ROL DEL SISTEMA
// ==========================================

    @PostMapping("/edit/{id}")
    @Transactional
    public String actualizarRol(
            @PathVariable Long id,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/roles";

        String rutaFormulario =
                "redirect:/admin/catalogos/roles/edit/" + id;

        // ======================================
        // BUSCAR ROL EXISTENTE
        // ======================================

        Rol rol = rolRepository.findById(id)
                .orElse(null);

        if (rol == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El rol solicitado no existe."
            );

            return rutaListado;
        }

        // ======================================
        // NORMALIZAR DATOS
        // ======================================

        codigo = codigo == null
                ? ""
                : codigo.trim().toUpperCase(java.util.Locale.ROOT);

        nombre = nombre == null
                ? ""
                : nombre.trim();

        descripcion = descripcion == null
                ? ""
                : descripcion.trim();

        // ======================================
        // VALIDAR CAMPOS OBLIGATORIOS
        // ======================================

        if (codigo.isBlank() || nombre.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El código y el nombre son obligatorios."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR LONGITUDES
        // ======================================

        if (codigo.length() > 50) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El código no puede superar los 50 caracteres."
            );

            return rutaFormulario;
        }

        if (nombre.length() > 100) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre no puede superar los 100 caracteres."
            );

            return rutaFormulario;
        }

        // ======================================
        // VALIDAR FORMATO DEL CÓDIGO
        // ======================================

        if (!codigo.matches("[A-Z][A-Z0-9_]*")) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El código debe comenzar con una letra "
                            + "y contener únicamente letras mayúsculas, "
                            + "números o guiones bajos."
            );

            return rutaFormulario;
        }

        // ======================================
        // VERIFICAR SI CAMBIÓ EL CÓDIGO
        // ======================================

        String codigoAnterior = rol.getCodigo();

        boolean cambioCodigo =
                !codigoAnterior.equalsIgnoreCase(codigo);

        boolean protegido =
                Boolean.TRUE.equals(rol.getProtegido());

        // ======================================
        // PROTEGER ROLES ORIGINALES
        // ======================================

        if (protegido && cambioCodigo) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede modificar el código "
                            + "de un rol protegido."
            );

            return rutaFormulario;
        }

        // ======================================
        // PROTEGER ROLES ASIGNADOS A USUARIOS
        // ======================================

        if (cambioCodigo
                && usuarioRepository.existsByRolIgnoreCase(
                codigoAnterior)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede modificar el código "
                            + "porque existen usuarios "
                            + "que tienen asignado este rol."
            );

            return rutaFormulario;
        }

        // ======================================
        // EVITAR CÓDIGOS DUPLICADOS
        // ======================================

        if (rolRepository.existsByCodigoIgnoreCaseAndIdNot(
                codigo, id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe otro rol con ese código."
            );

            return rutaFormulario;
        }

        // ======================================
        // ACTUALIZAR INFORMACIÓN
        // ======================================

        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);

        // No modificar los campos:
        // activo
        // protegido

        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        rolRepository.save(rol);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        registrarAuditoria(
                session,
                "EDITAR_ROL",
                "Actualizó el rol"
                        + " | ID: " + id
                        + " | Código anterior: " + codigoAnterior
                        + " | Código actual: " + codigo
                        + " | Nombre: " + nombre
        );

        // ======================================
        // CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Rol actualizado correctamente."
        );

        return rutaListado;
    }

    // ==========================================
// CU-14: DESACTIVAR ROL DEL SISTEMA
// ==========================================

    @PostMapping("/desactivar/{id}")
    @Transactional
    public String desactivarRol(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/roles";

        // ======================================
        // BUSCAR ROL POR ID
        // ======================================

        Rol rol = rolRepository.findById(id)
                .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (rol == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El rol solicitado no existe."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR SI YA ESTÁ INACTIVO
        // ======================================

        if (!Boolean.TRUE.equals(rol.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El rol ya se encuentra inactivo."
            );

            return rutaListado;
        }

        // ======================================
        // PROTEGER ROLES ORIGINALES
        // ======================================

        if (Boolean.TRUE.equals(rol.getProtegido())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede desactivar un rol protegido del sistema."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR USUARIOS ASIGNADOS
        // ======================================

        if (usuarioRepository.existsByRolIgnoreCase(
                rol.getCodigo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede desactivar este rol porque "
                            + "existen usuarios que lo tienen asignado."
            );

            return rutaListado;
        }

        // ======================================
        // DESACTIVAR ROL
        // ======================================

        rol.setActivo(false);

        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        rolRepository.save(rol);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        registrarAuditoria(
                session,
                "DESACTIVAR_ROL",
                "Desactivó el rol"
                        + " | ID: " + rol.getId()
                        + " | Código: " + rol.getCodigo()
                        + " | Nombre: " + rol.getNombre()
        );

        // ======================================
        // MOSTRAR CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Rol desactivado correctamente."
        );

        return rutaListado;
    }

    // ==========================================
// CU-14: ACTIVAR ROL DEL SISTEMA
// ==========================================

    @PostMapping("/activar/{id}")
    @Transactional
    public String activarRol(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ======================================
        // VALIDAR ADMINISTRADOR
        // ======================================

        if (!esAdministrador(session)) {
            return "redirect:/admin/login";
        }

        String rutaListado =
                "redirect:/admin/catalogos/roles";

        // ======================================
        // BUSCAR ROL POR ID
        // ======================================

        Rol rol = rolRepository.findById(id)
                .orElse(null);

        // ======================================
        // VALIDAR EXISTENCIA
        // ======================================

        if (rol == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El rol solicitado no existe."
            );

            return rutaListado;
        }

        // ======================================
        // VALIDAR SI YA ESTÁ ACTIVO
        // ======================================

        if (Boolean.TRUE.equals(rol.getActivo())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El rol ya se encuentra activo."
            );

            return rutaListado;
        }

        // ======================================
        // ACTIVAR ROL
        // ======================================

        rol.setActivo(true);

        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        rolRepository.save(rol);

        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        registrarAuditoria(
                session,
                "ACTIVAR_ROL",
                "Activó el rol"
                        + " | ID: " + rol.getId()
                        + " | Código: " + rol.getCodigo()
                        + " | Nombre: " + rol.getNombre()
        );

        // ======================================
        // MOSTRAR CONFIRMACIÓN
        // ======================================

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Rol activado correctamente."
        );

        return rutaListado;
    }

}