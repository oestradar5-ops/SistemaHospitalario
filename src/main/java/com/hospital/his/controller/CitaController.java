package com.hospital.his.controller;

import com.hospital.his.entity.*;
import com.hospital.his.repository.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Controller
public class CitaController {

    private final SucursalRepository sucursalRepository;
    private final SucursalEspecialidadRepository sucursalEspecialidadRepository;
    private final MedicoRepository medicoRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;


    public CitaController(
            SucursalRepository sucursalRepository,
            SucursalEspecialidadRepository sucursalEspecialidadRepository,
            MedicoRepository medicoRepository,
            HorarioMedicoRepository horarioMedicoRepository,
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository) {

        this.sucursalRepository = sucursalRepository;
        this.sucursalEspecialidadRepository = sucursalEspecialidadRepository;
        this.medicoRepository = medicoRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    // =====================================================
    // MOSTRAR ASISTENTE DE CITAS
    // =====================================================

    @GetMapping("/paciente/citas/agendar")
    public String mostrarAgendarCita(
            HttpSession session) {

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");


        if (nombreUsuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return "redirect:/login";
        }


        return "paciente/citas/agendar";
    }


    // =====================================================
    // PASO 1 - SUCURSALES ACTIVAS
    // =====================================================

    @ResponseBody
    @GetMapping("/api/citas/sucursales")
    public List<Map<String, Object>> obtenerSucursales() {

        return sucursalRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(sucursal -> Map.<String, Object>of(
                        "id", sucursal.getId(),
                        "nombre", sucursal.getNombre(),
                        "direccion",
                        sucursal.getDireccion() == null
                                ? ""
                                : sucursal.getDireccion()
                ))
                .toList();
    }


    // =====================================================
    // PASO 2 - ESPECIALIDADES POR SUCURSAL
    // =====================================================

    @ResponseBody
    @GetMapping("/api/citas/especialidades")
    public List<Map<String, Object>> obtenerEspecialidades(
            @RequestParam Long sucursalId) {

        return sucursalEspecialidadRepository
                .findBySucursalIdAndEspecialidadActivoTrueOrderByEspecialidadNombreAsc(
                        sucursalId
                )
                .stream()
                .map(relacion -> Map.<String, Object>of(
                        "id",
                        relacion.getEspecialidad().getId(),

                        "nombre",
                        relacion.getEspecialidad().getNombre()
                ))
                .toList();
    }


    // =====================================================
    // PASO 3 - MÉDICOS
    // =====================================================

    @ResponseBody
    @GetMapping("/api/citas/medicos")
    public List<Map<String, Object>> obtenerMedicos(

            @RequestParam Long sucursalId,

            @RequestParam Long especialidadId) {


        return medicoRepository
                .findBySucursalIdAndEspecialidadIdAndActivoTrue(
                        sucursalId,
                        especialidadId
                )
                .stream()
                .map(medico -> Map.<String, Object>of(
                        "id",
                        medico.getId(),

                        "nombre",
                        medico.getUsuario()
                                .getNombreCompleto()
                ))
                .toList();
    }


    // =====================================================
    // PASO 4 - HORARIOS DISPONIBLES
    // =====================================================

    @ResponseBody
    @GetMapping("/api/citas/horarios")
    public List<Map<String, Object>> obtenerHorarios(
            @RequestParam Long medicoId) {

        return horarioMedicoRepository
                .findByMedicoIdAndDisponibleTrueOrderByFechaAscHoraAsc(
                        medicoId
                )
                .stream()
                .map(horario -> Map.<String, Object>of(

                        "id",
                        horario.getId(),

                        "fecha",
                        horario.getFecha().toString(),

                        "hora",
                        horario.getHora().toString()
                ))
                .toList();
    }


    // =====================================================
    // PASO 5 - CONFIRMAR CITA
    // =====================================================

    @ResponseBody
    @PostMapping("/api/citas/confirmar")
    @Transactional
    public Map<String, Object> confirmarCita(

            @RequestBody Map<String, Object> datos,

            HttpSession session) {

        System.out.println("==========================================");
        System.out.println("ENTRÓ AL MÉTODO CONFIRMAR CITA");
        System.out.println("Datos recibidos: " + datos);
        System.out.println("Usuario sesión: " + session.getAttribute("usuario"));
        System.out.println("Rol sesión: " + session.getAttribute("rol"));
        System.out.println("==========================================");


        // =================================================
        // COMPROBAR PACIENTE
        // =================================================

        String nombreUsuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");


        if (nombreUsuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Debe iniciar sesión como paciente."
            );
        }


        // =================================================
        // LEER DATOS
        // =================================================

        Long sucursalId;
        Long especialidadId;
        Long medicoId;
        Long horarioId;
        String motivo;


        try {

            sucursalId =
                    Long.valueOf(
                            datos.get("sucursalId").toString()
                    );

            especialidadId =
                    Long.valueOf(
                            datos.get("especialidadId").toString()
                    );

            medicoId =
                    Long.valueOf(
                            datos.get("medicoId").toString()
                    );

            horarioId =
                    Long.valueOf(
                            datos.get("horarioId").toString()
                    );

            motivo =
                    datos.get("motivo").toString().trim();

        } catch (Exception e) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Los datos de la cita están incompletos."
            );
        }


        // =================================================
        // VALIDAR MOTIVO
        // =================================================

        if (motivo.length() < 10 ||
                motivo.length() > 2000) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El motivo de la consulta debe contener entre 10 y 2000 caracteres."
            );
        }


        // =================================================
        // BUSCAR PACIENTE
        // =================================================

        Usuario paciente =
                usuarioRepository
                        .findByNombreUsuario(nombreUsuario)
                        .orElse(null);


        if (paciente == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "No se encontró el paciente."
            );
        }


        // =================================================
        // BUSCAR DATOS SELECCIONADOS
        // =================================================

        Sucursal sucursal =
                sucursalRepository
                        .findById(sucursalId)
                        .orElse(null);

        Especialidad especialidad =
                sucursalEspecialidadRepository
                        .findBySucursalIdAndEspecialidadActivoTrueOrderByEspecialidadNombreAsc(
                                sucursalId
                        )
                        .stream()
                        .map(SucursalEspecialidad::getEspecialidad)
                        .filter(e ->
                                e.getId().equals(especialidadId))
                        .findFirst()
                        .orElse(null);

        Medico medico =
                medicoRepository
                        .findById(medicoId)
                        .orElse(null);

        HorarioMedico horario =
                horarioMedicoRepository
                        .findById(horarioId)
                        .orElse(null);


        if (sucursal == null ||
                especialidad == null ||
                medico == null ||
                horario == null) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "Uno de los datos seleccionados ya no está disponible."
            );
        }


        // =================================================
        // COMPROBAR RELACIONES
        // =================================================

        if (!medico.getSucursal().getId()
                .equals(sucursalId) ||

                !medico.getEspecialidad().getId()
                        .equals(especialidadId) ||

                !horario.getMedico().getId()
                        .equals(medicoId)) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "La selección de la cita no es válida."
            );
        }


        // =================================================
        // COMPROBAR DISPONIBILIDAD
        // =================================================

        if (!Boolean.TRUE.equals(
                horario.getDisponible())) {

            return Map.of(
                    "estado", "ERROR",
                    "mensaje",
                    "El horario seleccionado ya no se encuentra disponible."
            );
        }


        // =================================================
        // CREAR CITA
        // =================================================

        Cita cita = new Cita();

        cita.setPaciente(paciente);
        cita.setSucursal(sucursal);
        cita.setEspecialidad(especialidad);
        cita.setMedico(medico);
        cita.setHorario(horario);
        cita.setMotivoConsulta(motivo);

        cita.setEstado(
                "PENDIENTE_DE_PAGO"
        );

// La cita fue creada directamente
// desde el portal del paciente.
        cita.setOrigenCita(
                "PORTAL"
        );


        Cita citaGuardada =
                citaRepository.save(cita);


        // =================================================
        // RESERVAR HORARIO
        // =================================================

        horario.setDisponible(false);

        horarioMedicoRepository.save(horario);


        // =================================================
        // RESPUESTA
        // =================================================

        return Map.of(
                "estado", "OK",

                "mensaje",
                "Su cita ha sido registrada exitosamente. " +
                        "Será redirigido al proceso de pago " +
                        "para confirmar la reserva.",

                "citaId",
                citaGuardada.getId()
        );
    }
}