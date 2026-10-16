package com.hospital.his.controller;

import com.hospital.his.dto.FarmaciaSustitucionRequest;
import com.hospital.his.entity.AutorizacionSustitucion;
import com.hospital.his.service.FarmaciaSustitucionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/farmacia/api/sustituciones")
public class FarmaciaSustitucionController {

    private final FarmaciaSustitucionService sustitucionService;

    public FarmaciaSustitucionController(
            FarmaciaSustitucionService sustitucionService) {

        this.sustitucionService = sustitucionService;
    }

    // =====================================================
    // SOLICITAR SUSTITUCIÓN
    // =====================================================

    @PostMapping
    public ResponseEntity<?> solicitar(
            @RequestBody FarmaciaSustitucionRequest solicitud,
            HttpSession sesion) {

        try {

            AutorizacionSustitucion autorizacion =
                    sustitucionService.solicitarSustitucion(
                            solicitud.getRecetaMedicamentoId(),
                            solicitud.getMedicamentoSustitutoId(),
                            solicitud.getMotivo(),
                            sesion
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
                    "Solicitud enviada correctamente al médico."
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(resultado);

        } catch (SecurityException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // CONSULTAR SOLICITUDES DE LA SUCURSAL
    // =====================================================

    @GetMapping
    public ResponseEntity<?> listar(HttpSession sesion) {

        try {

            var solicitudes =
                    sustitucionService.listarSolicitudes(sesion);

            var resultado = solicitudes.stream()
                    .map(a -> {

                        Map<String, Object> datos =
                                new LinkedHashMap<>();

                        datos.put(
                                "autorizacionId",
                                a.getId()
                        );

                        datos.put(
                                "recetaMedicamentoId",
                                a.getRecetaMedicamento().getId()
                        );

                        datos.put(
                                "medicamentoSustitutoId",
                                a.getMedicamentoSustituto().getId()
                        );

                        datos.put(
                                "estado",
                                a.getEstado()
                        );

                        datos.put(
                                "motivo",
                                a.getMotivo()
                        );

                        return datos;
                    })
                    .toList();

            return ResponseEntity.ok(resultado);

        } catch (SecurityException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}