package com.hospital.his.controller;

import com.hospital.his.dto.MedicoSustitucionRespuestaRequest;
import com.hospital.his.entity.AutorizacionSustitucion;
import com.hospital.his.entity.Usuario;
import com.hospital.his.repository.UsuarioRepository;
import com.hospital.his.service.FarmaciaSustitucionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/medico/sustituciones")
public class MedicoSustitucionController {

    // =====================================================
    // REPOSITORIOS Y SERVICIOS
    // =====================================================

    private final UsuarioRepository usuarioRepository;

    private final FarmaciaSustitucionService sustitucionService;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MedicoSustitucionController(
            UsuarioRepository usuarioRepository,
            FarmaciaSustitucionService sustitucionService) {

        this.usuarioRepository = usuarioRepository;
        this.sustitucionService = sustitucionService;
    }

    // =====================================================
    // IDENTIFICAR MÉDICO AUTENTICADO
    // =====================================================

    private Usuario obtenerMedico(HttpSession sesion) {

        String nombreUsuario =
                (String) sesion.getAttribute("usuario");

        String rol =
                (String) sesion.getAttribute("rol");

        if (nombreUsuario == null ||
                nombreUsuario.isBlank() ||
                !"MEDICO".equalsIgnoreCase(rol)) {

            throw new SecurityException(
                    "Debe iniciar sesión como médico."
            );
        }

        Usuario medico = usuarioRepository
                .findByNombreUsuario(nombreUsuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "No se encontró el usuario médico."
                        )
                );

        // =================================================
        // CORRECCIÓN: getActivo() EN LUGAR DE isActivo()
        // =================================================

        if (!Boolean.TRUE.equals(medico.getActivo()) ||
                !"MEDICO".equalsIgnoreCase(medico.getRol())) {

            throw new SecurityException(
                    "El usuario no tiene permisos médicos."
            );
        }

        return medico;
    }

    // =====================================================
    // MOSTRAR PANTALLA DE AUTORIZACIONES MÉDICAS
    // =====================================================

    @GetMapping
    public String mostrarPantalla(
            HttpSession sesion,
            Model model) {

        try {

            Usuario medico = obtenerMedico(sesion);

            model.addAttribute(
                    "usuarioActual",
                    medico.getNombreUsuario()
            );

            return "medico/sustituciones";

        } catch (SecurityException e) {

            return "redirect:/medico/login";
        }
    }

    // =====================================================
    // CONSULTAR SOLICITUDES PENDIENTES DEL MÉDICO
    // =====================================================

    @GetMapping("/api/pendientes")
    @ResponseBody
    public ResponseEntity<?> listarPendientes(
            HttpSession sesion) {

        try {

            // Identificar al médico que inició sesión.
            Usuario medico = obtenerMedico(sesion);

            // Consultar únicamente las solicitudes
            // correspondientes a este médico.
            List<AutorizacionSustitucion> solicitudes =
                    sustitucionService.listarPendientesMedico(
                            medico
                    );

            // Convertir las solicitudes a JSON.
            List<Map<String, Object>> resultado =
                    solicitudes.stream()
                            .map(this::convertirSolicitud)
                            .toList();

            return ResponseEntity.ok(resultado);

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));
        }
    }

    // =====================================================
    // RESPONDER SOLICITUD
    // =====================================================

    @PostMapping("/api/{id}/responder")
    @ResponseBody
    public ResponseEntity<?> responderSolicitud(
            @PathVariable Long id,
            @RequestBody MedicoSustitucionRespuestaRequest solicitud,
            HttpSession sesion) {

        try {

            // Identificar al médico autenticado.
            Usuario medico = obtenerMedico(sesion);

            if (solicitud == null) {

                throw new IllegalArgumentException(
                        "Debe proporcionar una respuesta."
                );
            }

            // El servicio valida:
            // 1. Que sea un médico activo.
            // 2. Que sea el responsable de la receta.
            // 3. Que la solicitud esté pendiente.
            // 4. Que la receta esté vigente al autorizar.
            // 5. Que no se responda dos veces.

            AutorizacionSustitucion autorizacion =
                    sustitucionService.responderSolicitud(
                            id,
                            solicitud.getDecision(),
                            solicitud.getObservaciones(),
                            medico
                    );

            Map<String, Object> resultado =
                    new LinkedHashMap<>();

            resultado.put(
                    "autorizacionId",
                    autorizacion.getId()
            );

            resultado.put(
                    "estado",
                    autorizacion.getEstado()
            );

            resultado.put(
                    "mensaje",
                    "Solicitud respondida correctamente."
            );

            return ResponseEntity.ok(resultado);

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));
        }
    }

    // =====================================================
    // CONVERTIR SOLICITUD A JSON
    // =====================================================

    private Map<String, Object> convertirSolicitud(
            AutorizacionSustitucion autorizacion) {

        Map<String, Object> datos =
                new LinkedHashMap<>();

        // Identificador de la solicitud.
        datos.put(
                "autorizacionId",
                autorizacion.getId()
        );

        // Identificador del medicamento recetado.
        datos.put(
                "recetaMedicamentoId",
                autorizacion
                        .getRecetaMedicamento()
                        .getId()
        );

        // Medicamento original.
        datos.put(
                "medicamentoOriginal",
                autorizacion
                        .getRecetaMedicamento()
                        .getMedicamento()
                        .getNombre()
        );

        // Medicamento sustituto propuesto.
        datos.put(
                "medicamentoSustituto",
                autorizacion
                        .getMedicamentoSustituto()
                        .getNombre()
        );

        // Motivo de la sustitución.
        datos.put(
                "motivo",
                autorizacion.getMotivo()
        );

        // Estado de la solicitud.
        datos.put(
                "estado",
                autorizacion.getEstado()
        );

        // Fecha en que Farmacia solicitó el cambio.
        datos.put(
                "fechaSolicitud",
                autorizacion.getFechaSolicitud()
        );

        // Sucursal que realizó la solicitud.
        datos.put(
                "sucursal",
                autorizacion
                        .getSucursal()
                        .getNombre()
        );

        return datos;
    }
}