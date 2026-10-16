package com.hospital.his.service;

import com.hospital.his.entity.*;
import com.hospital.his.repository.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FarmaciaSustitucionService {

    private final AutorizacionSustitucionRepository autorizacionRepository;
    private final RecetaMedicamentoRepository recetaMedicamentoRepository;
    private final CatalogoMedicamentoRepository catalogoMedicamentoRepository;
    private final FarmaciaSesionService farmaciaSesionService;

    public FarmaciaSustitucionService(
            AutorizacionSustitucionRepository autorizacionRepository,
            RecetaMedicamentoRepository recetaMedicamentoRepository,
            CatalogoMedicamentoRepository catalogoMedicamentoRepository,
            FarmaciaSesionService farmaciaSesionService) {

        this.autorizacionRepository = autorizacionRepository;
        this.recetaMedicamentoRepository = recetaMedicamentoRepository;
        this.catalogoMedicamentoRepository = catalogoMedicamentoRepository;
        this.farmaciaSesionService = farmaciaSesionService;
    }

    // =====================================================
    // VALIDAR VIGENCIA Y ESTADO DE LA RECETA
    // =====================================================

    private void validarReceta(RecetaMedica receta) {

        if (receta == null) {
            throw new IllegalArgumentException(
                    "No se encontró la receta médica."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (receta.getFechaCreacion() == null ||
                receta.getFechaCreacion().isAfter(ahora) ||
                ahora.isAfter(receta.getFechaCreacion().plusDays(7))) {

            throw new IllegalStateException(
                    "La receta médica no está vigente."
            );
        }

        if (!"PENDIENTE".equals(receta.getEstado()) &&
                !"PARCIALMENTE_DESPACHADA".equals(receta.getEstado())) {

            throw new IllegalStateException(
                    "La receta no permite nuevas sustituciones."
            );
        }
    }

    // =====================================================
    // VALIDAR MÉDICO RESPONSABLE
    // =====================================================

    private void validarMedicoResponsable(
            RecetaMedica receta,
            Usuario medicoAutenticado) {

        if (receta.getCita() == null ||
                receta.getCita().getMedico() == null ||
                receta.getCita().getMedico().getUsuario() == null) {

            throw new IllegalStateException(
                    "La receta no tiene un médico responsable asignado."
            );
        }

        Usuario medicoResponsable =
                receta.getCita()
                        .getMedico()
                        .getUsuario();

        if (!Objects.equals(
                medicoResponsable.getId(),
                medicoAutenticado.getId())) {

            throw new SecurityException(
                    "No tiene autorización para responder esta solicitud. " +
                            "La receta pertenece a otro médico."
            );
        }
    }

    // =====================================================
    // SOLICITAR SUSTITUCIÓN DESDE FARMACIA
    // =====================================================

    @Transactional
    public AutorizacionSustitucion solicitarSustitucion(
            Long recetaMedicamentoId,
            Long medicamentoSustitutoId,
            String motivo,
            HttpSession sesion) {

        Usuario empleado =
                farmaciaSesionService.obtenerUsuario(sesion);

        Sucursal sucursal =
                farmaciaSesionService.obtenerSucursal(sesion);

        if (recetaMedicamentoId == null ||
                medicamentoSustitutoId == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar los medicamentos."
            );
        }

        if (motivo == null || motivo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe indicar el motivo de la sustitución."
            );
        }

        String motivoLimpio = motivo.trim();

        if (motivoLimpio.length() > 1000) {

            throw new IllegalArgumentException(
                    "El motivo no puede superar 1000 caracteres."
            );
        }

        RecetaMedicamento recetaMedicamento =
                recetaMedicamentoRepository
                        .findById(recetaMedicamentoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró el medicamento recetado."
                                )
                        );

        CatalogoMedicamento sustituto =
                catalogoMedicamentoRepository
                        .findById(medicamentoSustitutoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró el medicamento sustituto."
                                )
                        );

        if (Objects.equals(
                recetaMedicamento.getMedicamento().getId(),
                sustituto.getId())) {

            throw new IllegalArgumentException(
                    "El medicamento sustituto debe ser diferente al recetado."
            );
        }

        RecetaMedica receta = recetaMedicamento.getReceta();

        validarReceta(receta);

        // Evitar solicitudes pendientes duplicadas.

        boolean solicitudPendiente =
                autorizacionRepository
                        .findByRecetaMedicamentoIdOrderByFechaSolicitudDesc(
                                recetaMedicamentoId
                        )
                        .stream()
                        .anyMatch(a ->
                                "PENDIENTE".equals(a.getEstado()) &&
                                        Objects.equals(
                                                a.getMedicamentoSustituto().getId(),
                                                medicamentoSustitutoId
                                        )
                        );

        if (solicitudPendiente) {

            throw new IllegalStateException(
                    "Ya existe una solicitud pendiente para esta sustitución."
            );
        }

        AutorizacionSustitucion autorizacion =
                new AutorizacionSustitucion();

        autorizacion.setRecetaMedicamento(recetaMedicamento);
        autorizacion.setMedicamentoSustituto(sustituto);
        autorizacion.setSolicitadoPor(empleado);
        autorizacion.setSucursal(sucursal);
        autorizacion.setMotivo(motivoLimpio);
        autorizacion.setEstado("PENDIENTE");

        return autorizacionRepository.save(autorizacion);
    }

    // =====================================================
    // CONSULTAR SOLICITUDES DE FARMACIA
    // =====================================================

    @Transactional(readOnly = true)
    public List<AutorizacionSustitucion> listarSolicitudes(
            HttpSession sesion) {

        Sucursal sucursal =
                farmaciaSesionService.obtenerSucursal(sesion);

        return autorizacionRepository
                .findBySucursalIdOrderByFechaSolicitudDesc(
                        sucursal.getId()
                );
    }

    // =====================================================
    // CONSULTAR SOLICITUDES PENDIENTES DEL MÉDICO
    // =====================================================

    @Transactional(readOnly = true)
    public List<AutorizacionSustitucion> listarPendientesMedico(
            Usuario medicoAutenticado) {

        validarUsuarioMedico(medicoAutenticado);

        return autorizacionRepository
                .findByEstadoOrderByFechaSolicitudAsc("PENDIENTE")
                .stream()
                .filter(autorizacion -> {

                    RecetaMedica receta =
                            autorizacion.getRecetaMedicamento().getReceta();

                    if (receta == null ||
                            receta.getCita() == null ||
                            receta.getCita().getMedico() == null ||
                            receta.getCita().getMedico().getUsuario() == null) {

                        return false;
                    }

                    Usuario responsable =
                            receta.getCita().getMedico().getUsuario();

                    return Objects.equals(
                            responsable.getId(),
                            medicoAutenticado.getId()
                    );
                })
                .toList();
    }

    // =====================================================
    // VALIDAR USUARIO MÉDICO
    // CORREGIDO: getActivo() EN LUGAR DE isActivo()
    // =====================================================

    private void validarUsuarioMedico(Usuario medico) {

        if (medico == null ||
                !Boolean.TRUE.equals(medico.getActivo()) ||
                !"MEDICO".equalsIgnoreCase(medico.getRol())) {

            throw new SecurityException(
                    "Solo un médico activo puede responder solicitudes."
            );
        }
    }

    // =====================================================
    // RESPONDER SOLICITUD DESDE EL PANEL MÉDICO
    // =====================================================

    @Transactional
    public AutorizacionSustitucion responderSolicitud(
            Long autorizacionId,
            String decision,
            String observaciones,
            Usuario medicoAutenticado) {

        validarUsuarioMedico(medicoAutenticado);

        if (autorizacionId == null || autorizacionId <= 0) {

            throw new IllegalArgumentException(
                    "Debe indicar una autorización válida."
            );
        }

        if (!"AUTORIZADA".equals(decision) &&
                !"RECHAZADA".equals(decision)) {

            throw new IllegalArgumentException(
                    "La decisión debe ser AUTORIZADA o RECHAZADA."
            );
        }

        String observacionesLimpias =
                observaciones == null ? "" : observaciones.trim();

        if (observacionesLimpias.length() > 1000) {

            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 1000 caracteres."
            );
        }

        if ("RECHAZADA".equals(decision) &&
                observacionesLimpias.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe indicar el motivo del rechazo."
            );
        }

        AutorizacionSustitucion autorizacion =
                autorizacionRepository
                        .bloquearPorId(autorizacionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la solicitud."
                                )
                        );

        if (!"PENDIENTE".equals(autorizacion.getEstado())) {

            throw new IllegalStateException(
                    "Esta solicitud ya fue respondida."
            );
        }

        RecetaMedica receta =
                autorizacion.getRecetaMedicamento().getReceta();

        // Seguridad: solamente el médico responsable.

        validarMedicoResponsable(receta, medicoAutenticado);

        // La receta debe seguir vigente para autorizar.

        if ("AUTORIZADA".equals(decision)) {
            validarReceta(receta);
        }

        autorizacion.setEstado(decision);
        autorizacion.setAutorizadoPor(medicoAutenticado);
        autorizacion.setObservacionesMedico(observacionesLimpias);
        autorizacion.setFechaRespuesta(LocalDateTime.now());

        return autorizacionRepository.save(autorizacion);
    }
}