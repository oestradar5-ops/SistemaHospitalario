package com.hospital.his.controller;

import com.hospital.his.entity.Cita;
import com.hospital.his.entity.SignosVitales;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.CitaRepository;
import com.hospital.his.repository.SignosVitalesRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;


@Controller
public class SignosVitalesController {

    private final CitaRepository citaRepository;

    private final SignosVitalesRepository
            signosVitalesRepository;

    private final UsuarioRepository
            usuarioRepository;


    public SignosVitalesController(
            CitaRepository citaRepository,
            SignosVitalesRepository signosVitalesRepository,
            UsuarioRepository usuarioRepository) {

        this.citaRepository =
                citaRepository;

        this.signosVitalesRepository =
                signosVitalesRepository;

        this.usuarioRepository =
                usuarioRepository;
    }


    // =====================================================
    // VALIDAR ENFERMERO
    // =====================================================

    private boolean esEnfermero(
            HttpSession session) {

        String usuario =
                (String) session
                        .getAttribute("usuario");

        String rol =
                (String) session
                        .getAttribute("rol");


        return usuario != null
                &&
                "ENFERMERO"
                        .equalsIgnoreCase(rol);
    }


    // =====================================================
    // MOSTRAR FORMULARIO
    // =====================================================

    @GetMapping(
            "/enfermeria/signos/{citaId}"
    )
    public String mostrarFormulario(
            @PathVariable Long citaId,
            HttpSession session,
            Model model) {


        if (!esEnfermero(session)) {

            return "redirect:/enfermeria/login";
        }


        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElse(null);


        if (cita == null) {

            return "redirect:/enfermeria";
        }


        if (!"SIGNOS_VITALES"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return "redirect:/enfermeria";
        }


        model.addAttribute(
                "cita",
                cita
        );


        model.addAttribute(
                "usuarioActual",
                session.getAttribute("usuario")
        );


        return "enfermeria/signos";
    }


    // =====================================================
    // REGISTRAR SIGNOS VITALES
    // =====================================================

    @ResponseBody
    @PostMapping(
            "/api/enfermeria/signos/{citaId}"
    )
    @Transactional
    public Map<String, Object> registrarSignos(
            @PathVariable Long citaId,
            @RequestBody Map<String, Object> datos,
            HttpSession session) {


        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        if (!esEnfermero(session)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No tiene permisos para realizar esta operación."
            );
        }


        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElse(null);


        if (cita == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no existe."
            );
        }


        if (!"SIGNOS_VITALES"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La cita no se encuentra en proceso de toma de signos vitales."
            );
        }


        // =================================================
        // EVITAR REGISTRO DUPLICADO
        // =================================================

        if (signosVitalesRepository
                .existsByCitaId(citaId)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Esta cita ya tiene signos vitales registrados."
            );
        }


        // =================================================
        // BUSCAR ENFERMERO
        // =================================================

        String nombreUsuario =
                (String) session
                        .getAttribute("usuario");


        Usuario enfermero =
                usuarioRepository
                        .findByNombreUsuario(
                                nombreUsuario
                        )
                        .orElse(null);


        if (enfermero == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No se encontró el usuario de enfermería."
            );
        }


        // =================================================
        // LEER DATOS
        // =================================================

        Integer sistolica;

        Integer diastolica;

        BigDecimal temperatura;

        BigDecimal peso;

        BigDecimal talla;

        Integer frecuenciaCardiaca;

        Boolean esEmergencia;


        try {

            sistolica =
                    Integer.valueOf(
                            datos.get(
                                    "presionSistolica"
                            ).toString()
                    );


            diastolica =
                    Integer.valueOf(
                            datos.get(
                                    "presionDiastolica"
                            ).toString()
                    );


            temperatura =
                    new BigDecimal(
                            datos.get(
                                    "temperatura"
                            ).toString()
                    );


            peso =
                    new BigDecimal(
                            datos.get(
                                    "peso"
                            ).toString()
                    );


            talla =
                    new BigDecimal(
                            datos.get(
                                    "talla"
                            ).toString()
                    );


            frecuenciaCardiaca =
                    Integer.valueOf(
                            datos.get(
                                    "frecuenciaCardiaca"
                            ).toString()
                    );


            Object emergenciaDato =
                    datos.get(
                            "esEmergencia"
                    );


            esEmergencia =
                    emergenciaDato != null
                            &&
                            Boolean.parseBoolean(
                                    emergenciaDato
                                            .toString()
                            );

        }

        catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Debe completar correctamente todos los campos de signos vitales."
            );
        }


        // =================================================
        // VALIDAR PRESIÓN SISTÓLICA
        // 60 - 250
        // =================================================

        if (sistolica < 60 ||
                sistolica > 250) {

            return Map.of(
                    "estado", "ERROR",
                    "campo", "presionSistolica",
                    "mensaje",
                    "La presión sistólica debe estar entre 60 y 250 mmHg."
            );
        }


        // =================================================
        // VALIDAR PRESIÓN DIASTÓLICA
        // 40 - 150
        // =================================================

        if (diastolica < 40 ||
                diastolica > 150) {

            return Map.of(
                    "estado", "ERROR",
                    "campo", "presionDiastolica",
                    "mensaje",
                    "La presión diastólica debe estar entre 40 y 150 mmHg."
            );
        }


        // =================================================
        // VALIDAR TEMPERATURA
        // 34 - 42 °C
        // =================================================

        if (temperatura.compareTo(
                new BigDecimal("34.0")
        ) < 0 ||

                temperatura.compareTo(
                        new BigDecimal("42.0")
                ) > 0) {

            return Map.of(
                    "estado", "ERROR",
                    "campo", "temperatura",
                    "mensaje",
                    "La temperatura debe estar entre 34.0 y 42.0 °C."
            );
        }


        // =================================================
        // VALIDAR PESO
        // 0.5 - 300 kg
        // =================================================

        if (peso.compareTo(
                new BigDecimal("0.5")
        ) < 0 ||

                peso.compareTo(
                        new BigDecimal("300")
                ) > 0) {

            return Map.of(
                    "estado", "ERROR",
                    "campo", "peso",
                    "mensaje",
                    "El peso debe estar entre 0.5 y 300 kg."
            );
        }


        // =================================================
        // VALIDAR TALLA
        // 30 - 250 cm
        // =================================================

        if (talla.compareTo(
                new BigDecimal("30")
        ) < 0 ||

                talla.compareTo(
                        new BigDecimal("250")
                ) > 0) {

            return Map.of(
                    "estado", "ERROR",
                    "campo", "talla",
                    "mensaje",
                    "La talla debe estar entre 30 y 250 cm."
            );
        }


        // =================================================
        // VALIDAR FRECUENCIA CARDÍACA
        // 30 - 220
        // =================================================

        if (frecuenciaCardiaca < 30 ||
                frecuenciaCardiaca > 220) {

            return Map.of(
                    "estado", "ERROR",
                    "campo", "frecuenciaCardiaca",
                    "mensaje",
                    "La frecuencia cardíaca debe estar entre 30 y 220 latidos por minuto."
            );
        }


        // =================================================
        // DETECTAR ALERTA CLÍNICA
        // =================================================

        boolean alertaPresion =
                sistolica < 90
                        ||
                        sistolica > 140
                        ||
                        diastolica < 60
                        ||
                        diastolica > 90;


        boolean alertaTemperatura =
                temperatura.compareTo(
                        new BigDecimal("36.0")
                ) < 0
                        ||
                        temperatura.compareTo(
                                new BigDecimal("37.5")
                        ) > 0;


        boolean alertaFrecuencia =
                frecuenciaCardiaca < 60
                        ||
                        frecuenciaCardiaca > 100;


        boolean alertaClinica =
                alertaPresion
                        ||
                        alertaTemperatura
                        ||
                        alertaFrecuencia;


        // =================================================
        // CREAR REGISTRO
        // =================================================

        SignosVitales signos =
                new SignosVitales();


        signos.setCita(
                cita
        );


        signos.setEnfermero(
                enfermero
        );


        signos.setPresionSistolica(
                sistolica
        );


        signos.setPresionDiastolica(
                diastolica
        );


        signos.setTemperatura(
                temperatura
        );


        signos.setPeso(
                peso
        );


        signos.setTalla(
                talla
        );


        signos.setFrecuenciaCardiaca(
                frecuenciaCardiaca
        );


        signos.setEsEmergencia(
                esEmergencia
        );


        signos.setAlertaClinica(
                alertaClinica
        );


        signosVitalesRepository.save(
                signos
        );


        // =================================================
        // ACTUALIZAR CITA
        // =================================================

        cita.setEstado(
                "EN_ESPERA"
        );


        if (Boolean.TRUE.equals(
                esEmergencia
        )) {

            cita.setPrioridad(
                    "EMERGENCIA"
            );
        }


        citaRepository.save(
                cita
        );


        // =================================================
        // RESPUESTA EMERGENCIA
        // =================================================

        if (Boolean.TRUE.equals(
                esEmergencia
        )) {

            return Map.of(
                    "estado", "OK",

                    "emergencia", true,

                    "alertaClinica",
                    alertaClinica,

                    "mensaje",
                    "Signos vitales de emergencia registrados para paciente " +
                            cita.getPaciente()
                                    .getNombreCompleto() +
                            ". El paciente debe pasar directamente a consulta médica."
            );
        }


        // =================================================
        // RESPUESTA NORMAL
        // =================================================

        return Map.of(
                "estado", "OK",

                "emergencia", false,

                "alertaClinica",
                alertaClinica,

                "mensaje",
                "Signos vitales del paciente " +
                        cita.getPaciente()
                                .getNombreCompleto() +
                        " registrados correctamente. " +
                        "El paciente puede regresar a la sala de espera."
        );
    }
}