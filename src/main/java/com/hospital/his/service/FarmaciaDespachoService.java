package com.hospital.his.service;

import com.hospital.his.dto.FarmaciaDespachoRequest;
import com.hospital.his.dto.FarmaciaDetalleRequest;

import com.hospital.his.entity.*;

import com.hospital.his.repository.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FarmaciaDespachoService {

    // ==========================================
    // SERVICIOS
    // ==========================================

    private final FarmaciaSesionService farmaciaSesionService;
    private final FarmaciaRecetaService farmaciaRecetaService;

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final RecetaMedicaRepository recetaRepository;
    private final DetalleDespachoRepository detalleRepository;
    private final DespachoMedicamentoRepository despachoRepository;
    private final InventarioMedicamentoRepository inventarioRepository;
    private final AutorizacionSustitucionRepository autorizacionRepository;
    private final CatalogoMedicamentoRepository medicamentoRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaDespachoService(
            FarmaciaSesionService farmaciaSesionService,
            FarmaciaRecetaService farmaciaRecetaService,
            RecetaMedicaRepository recetaRepository,
            DetalleDespachoRepository detalleRepository,
            DespachoMedicamentoRepository despachoRepository,
            InventarioMedicamentoRepository inventarioRepository,
            AutorizacionSustitucionRepository autorizacionRepository,
            CatalogoMedicamentoRepository medicamentoRepository) {

        this.farmaciaSesionService = farmaciaSesionService;
        this.farmaciaRecetaService = farmaciaRecetaService;
        this.recetaRepository = recetaRepository;
        this.detalleRepository = detalleRepository;
        this.despachoRepository = despachoRepository;
        this.inventarioRepository = inventarioRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    // ==========================================
    // CU-11: CONFIRMAR DESPACHO
    // ==========================================

    @Transactional
    public DespachoMedicamento confirmarDespacho(
            FarmaciaDespachoRequest solicitud,
            HttpSession sesion) {

        // ======================================
        // 1. VALIDAR SESIÓN
        // ======================================

        Usuario empleado =
                farmaciaSesionService.obtenerUsuario(sesion);

        Sucursal sucursal =
                farmaciaSesionService.obtenerSucursal(sesion);

        // ======================================
        // 2. VALIDAR SOLICITUD
        // ======================================

        if (solicitud == null
                || solicitud.getRecetaId() == null
                || solicitud.getRecetaId() <= 0) {

            throw new IllegalArgumentException(
                    "Debe indicar una receta médica válida."
            );
        }

        if (solicitud.getDetalles() == null
                || solicitud.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un medicamento."
            );
        }

        // ======================================
        // 3. BLOQUEAR RECETA
        // ======================================

        RecetaMedica receta = recetaRepository
                .bloquearRecetaParaDespacho(
                        solicitud.getRecetaId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró la receta médica."
                        )
                );

        // ======================================
        // 4. VALIDAR RECETA
        // ======================================

        farmaciaRecetaService.validarRecetaParaDespacho(
                receta
        );

        // ======================================
        // 5. PREPARAR DESPACHO
        // ======================================

        DespachoMedicamento despacho =
                new DespachoMedicamento();

        despacho.setReceta(receta);
        despacho.setSucursal(sucursal);
        despacho.setUsuario(empleado);
        despacho.setEstado("CONFIRMADO");

        BigDecimal montoTotal = BigDecimal.ZERO;

        Set<Long> detallesProcesados = new HashSet<>();

        // ======================================
        // 6. PROCESAR MEDICAMENTOS
        // ======================================

        for (FarmaciaDetalleRequest item :
                solicitud.getDetalles()) {

            if (item == null
                    || item.getRecetaMedicamentoId() == null
                    || item.getMedicamentoEntregadoId() == null
                    || item.getCantidad() == null) {

                throw new IllegalArgumentException(
                        "Los datos del medicamento están incompletos."
                );
            }

            if (item.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            // ==================================
            // EVITAR MEDICAMENTOS REPETIDOS
            // ==================================

            if (!detallesProcesados.add(
                    item.getRecetaMedicamentoId())) {

                throw new IllegalArgumentException(
                        "Un medicamento de la receta está repetido."
                );
            }

            // ==================================
            // BUSCAR MEDICAMENTO RECETADO
            // ==================================

            RecetaMedicamento recetaMedicamento =
                    receta.getMedicamentos()
                            .stream()
                            .filter(rm ->
                                    Objects.equals(
                                            rm.getId(),
                                            item.getRecetaMedicamentoId()
                                    )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "El medicamento no pertenece "
                                                    + "a la receta seleccionada."
                                    )
                            );

            // ==================================
            // VALIDAR CANTIDAD PENDIENTE
            // ==================================

            Long cantidadAnterior =
                    detalleRepository.obtenerCantidadDespachada(
                            recetaMedicamento.getId()
                    );

            long entregada =
                    cantidadAnterior == null
                            ? 0L
                            : cantidadAnterior;

            if (recetaMedicamento.getCantidad() == null
                    || recetaMedicamento.getCantidad() <= 0) {

                throw new IllegalStateException(
                        "La receta contiene una cantidad inválida."
                );
            }

            long pendiente =
                    recetaMedicamento.getCantidad() - entregada;

            if (pendiente <= 0) {

                throw new IllegalStateException(
                        "Este medicamento ya fue despachado completamente."
                );
            }

            if (item.getCantidad() > pendiente) {

                throw new IllegalStateException(
                        "La cantidad solicitada supera "
                                + "la cantidad pendiente: " + pendiente
                );
            }

            // ==================================
            // IDENTIFICAR MEDICAMENTO ENTREGADO
            // ==================================

            CatalogoMedicamento medicamentoRecetado =
                    recetaMedicamento.getMedicamento();

            CatalogoMedicamento medicamentoEntregado =
                    medicamentoRepository.findById(
                                    item.getMedicamentoEntregadoId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "El medicamento que desea entregar "
                                                    + "no existe."
                                    )
                            );

            boolean sustituido =
                    !Objects.equals(
                            medicamentoRecetado.getId(),
                            medicamentoEntregado.getId()
                    );

            // ==================================
            // 7. VALIDAR SUSTITUCIÓN MÉDICA
            // ==================================

            if (sustituido) {

                // Consultar las autorizaciones del
                // medicamento recetado, ordenadas
                // desde la más reciente.

                List<AutorizacionSustitucion> autorizaciones =
                        autorizacionRepository
                                .findByRecetaMedicamentoIdOrderByFechaSolicitudDesc(
                                        recetaMedicamento.getId()
                                );

                // Verificar que existan solicitudes.

                if (autorizaciones == null
                        || autorizaciones.isEmpty()) {

                    throw new IllegalStateException(
                            "La sustitución del medicamento "
                                    + "no cuenta con autorización médica."
                    );
                }

                // ==================================
                // UTILIZAR LA SOLICITUD MÁS RECIENTE
                // ==================================

                AutorizacionSustitucion ultimaAutorizacion =
                        autorizaciones.stream()
                                .max(
                                        Comparator
                                                .comparing(
                                                        AutorizacionSustitucion::getFechaSolicitud,
                                                        Comparator.nullsFirst(
                                                                Comparator.naturalOrder()
                                                        )
                                                )
                                                .thenComparing(
                                                        AutorizacionSustitucion::getId,
                                                        Comparator.nullsFirst(
                                                                Comparator.naturalOrder()
                                                        )
                                                )
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "No se encontró una autorización válida."
                                        )
                                );

                // ==================================
                // COMPROBAR ESTADO AUTORIZADA
                // ==================================

                boolean estadoAutorizado =
                        "AUTORIZADA".equalsIgnoreCase(
                                ultimaAutorizacion.getEstado()
                        );

                // ==================================
                // COMPROBAR MEDICAMENTO SUSTITUTO
                // ==================================

                boolean medicamentoCorrecto =
                        ultimaAutorizacion.getMedicamentoSustituto() != null
                                && Objects.equals(
                                ultimaAutorizacion
                                        .getMedicamentoSustituto()
                                        .getId(),
                                medicamentoEntregado.getId()
                        );

                // ==================================
                // COMPROBAR SUCURSAL
                // ==================================

                boolean sucursalCorrecta =
                        ultimaAutorizacion.getSucursal() != null
                                && Objects.equals(
                                ultimaAutorizacion
                                        .getSucursal()
                                        .getId(),
                                sucursal.getId()
                        );

                // ==================================
                // COMPROBAR MÉDICO AUTORIZANTE
                // ==================================

                boolean medicoAutorizante =
                        ultimaAutorizacion.getAutorizadoPor() != null;

                // ==================================
                // VALIDACIÓN FINAL
                // ==================================

                if (!estadoAutorizado
                        || !medicamentoCorrecto
                        || !sucursalCorrecta
                        || !medicoAutorizante) {

                    throw new IllegalStateException(
                            "La sustitución del medicamento "
                                    + "no cuenta con una autorización "
                                    + "médica vigente y válida."
                    );
                }
            }

            // ==================================
            // 8. BLOQUEAR INVENTARIO
            // ==================================

            InventarioMedicamento inventario =
                    inventarioRepository.buscarParaDespacho(
                                    medicamentoEntregado.getId(),
                                    sucursal.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "El medicamento no está disponible "
                                                    + "en el inventario de esta sucursal."
                                    )
                            );

            // ==================================
            // 9. VERIFICAR EXISTENCIAS
            // ==================================

            if (inventario.getCantidadDisponible() == null
                    || inventario.getCantidadDisponible()
                    < item.getCantidad()) {

                throw new IllegalStateException(
                        "No hay existencias suficientes de "
                                + medicamentoEntregado.getNombre()
                );
            }

            // ==================================
            // 10. CALCULAR PRECIO
            // ==================================

            BigDecimal precioUnitario =
                    inventario.getPrecioUnitario();

            if (precioUnitario == null
                    || precioUnitario.signum() < 0) {

                throw new IllegalStateException(
                        "El medicamento tiene un precio inválido."
                );
            }

            BigDecimal subtotal =
                    precioUnitario.multiply(
                            BigDecimal.valueOf(
                                    item.getCantidad()
                            )
                    );

            // ==================================
            // 11. DESCONTAR INVENTARIO
            // ==================================

            inventario.setCantidadDisponible(
                    inventario.getCantidadDisponible()
                            - item.getCantidad()
            );

            inventarioRepository.save(inventario);

            // ==================================
            // 12. CREAR DETALLE DEL DESPACHO
            // ==================================

            DetalleDespacho detalle =
                    new DetalleDespacho();

            detalle.setDespacho(despacho);
            detalle.setRecetaMedicamento(recetaMedicamento);
            detalle.setMedicamentoRecetado(medicamentoRecetado);
            detalle.setMedicamentoEntregado(medicamentoEntregado);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            detalle.setSustituido(sustituido);

            detalle.setRazonSustitucion(
                    sustituido
                            ? item.getRazonSustitucion()
                            : null
            );

            despacho.getDetalles().add(detalle);

            montoTotal = montoTotal.add(subtotal);
        }

        // ======================================
        // 13. CALCULAR TOTAL
        // ======================================

        despacho.setMontoTotal(montoTotal);

        // ======================================
        // 14. GUARDAR DESPACHO
        // ======================================

        DespachoMedicamento despachoGuardado =
                despachoRepository.saveAndFlush(despacho);

        // ======================================
        // 15. VERIFICAR SI LA RECETA SE COMPLETÓ
        // ======================================

        boolean recetaCompleta = true;

        for (RecetaMedicamento medicamento :
                receta.getMedicamentos()) {

            Long cantidadDespachada =
                    detalleRepository.obtenerCantidadDespachada(
                            medicamento.getId()
                    );

            long cantidadEntregada =
                    cantidadDespachada == null
                            ? 0L
                            : cantidadDespachada;

            if (medicamento.getCantidad() == null
                    || cantidadEntregada < medicamento.getCantidad()) {

                recetaCompleta = false;
                break;
            }
        }

        // ======================================
        // 16. ACTUALIZAR ESTADO DE LA RECETA
        // ======================================

        receta.setEstado(
                recetaCompleta
                        ? "DESPACHADA"
                        : "PARCIALMENTE_DESPACHADA"
        );

        recetaRepository.save(receta);

        return despachoGuardado;
    }
}