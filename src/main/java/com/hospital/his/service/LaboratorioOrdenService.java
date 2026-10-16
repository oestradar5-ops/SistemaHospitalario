package com.hospital.his.service;

import com.hospital.his.entity.OrdenLaboratorio;
import com.hospital.his.repository.OrdenLaboratorioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LaboratorioOrdenService {

    private final OrdenLaboratorioRepository ordenRepository;

    public LaboratorioOrdenService(
            OrdenLaboratorioRepository ordenRepository) {

        this.ordenRepository = ordenRepository;
    }

    // ==========================================
    // CU-09: LISTAR ÓRDENES
    // ==========================================

    @Transactional(readOnly = true)
    public List<OrdenLaboratorio> listarOrdenes() {

        return ordenRepository
                .findAllByOrderByFechaCreacionDesc();
    }

    // ==========================================
    // CU-09: BUSCAR POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public OrdenLaboratorio buscarPorId(Long id) {

        return ordenRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró la orden: " + id
                        )
                );
    }

    // ==========================================
    // CU-09: OBTENER DATOS PARA EL PANEL
    // ==========================================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosPanel() {

        List<OrdenLaboratorio> ordenes =
                ordenRepository.findAllByOrderByFechaCreacionDesc();

        List<Map<String, Object>> filas = new ArrayList<>();

        long pendientes = 0;
        long enProceso = 0;
        long completadas = 0;
        long resultadosPublicados = 0;

        for (OrdenLaboratorio orden : ordenes) {

            String estado = orden.getEstado();

            if ("PENDIENTE".equalsIgnoreCase(estado)) {
                pendientes++;
            }

            if ("EN_PROCESO".equalsIgnoreCase(estado)) {
                enProceso++;
            }

            if ("COMPLETADA".equalsIgnoreCase(estado)) {
                completadas++;
            }

            resultadosPublicados += orden.getExamenes()
                    .stream()
                    .filter(examen ->
                            Boolean.TRUE.equals(examen.getPublicado()))
                    .count();

            String paciente = orden.getCita()
                    .getPaciente()
                    .getNombreCompleto();

            String medico = orden.getCita()
                    .getMedico()
                    .getUsuario()
                    .getNombreCompleto();

            boolean externa =
                    Boolean.TRUE.equals(orden.getOrdenExterna());

            boolean pagada = orden.getFechaPago() != null;

            Map<String, Object> fila = new LinkedHashMap<>();

            fila.put("id", orden.getId());
            fila.put("paciente", paciente);
            fila.put("medico", medico);
            fila.put("fecha", orden.getFechaCreacion());
            fila.put("estado", estado);
            fila.put("externa", externa);
            fila.put("pagada", pagada);
            fila.put("monto", orden.getMontoTotal());

            filas.add(fila);
        }

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("ordenes", filas);
        datos.put("pendientes", pendientes);
        datos.put("enProceso", enProceso);
        datos.put("completadas", completadas);
        datos.put("resultadosPublicados", resultadosPublicados);

        return datos;
    }

    // ==========================================
    // CU-09: CONTADORES
    // ==========================================

    @Transactional(readOnly = true)
    public long contarPendientes() {

        return ordenRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .filter(orden ->
                        "PENDIENTE".equalsIgnoreCase(orden.getEstado()))
                .count();
    }

    @Transactional(readOnly = true)
    public long contarEnProceso() {

        return ordenRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .filter(orden ->
                        "EN_PROCESO".equalsIgnoreCase(orden.getEstado()))
                .count();
    }

    @Transactional(readOnly = true)
    public long contarCompletadas() {

        return ordenRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .filter(orden ->
                        "COMPLETADA".equalsIgnoreCase(orden.getEstado()))
                .count();
    }
    // ==========================================
// CU-09: CONSULTAR DETALLE DE UNA ORDEN
// ==========================================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDetalleOrden(Long ordenId) {

        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la orden de laboratorio: " + ordenId
                        )
                );

        Map<String, Object> detalle = new LinkedHashMap<>();

        // INFORMACIÓN GENERAL

        detalle.put("id", orden.getId());

        detalle.put(
                "paciente",
                orden.getCita().getPaciente().getNombreCompleto()
        );

        detalle.put(
                "medico",
                orden.getCita().getMedico()
                        .getUsuario().getNombreCompleto()
        );

        detalle.put("fecha", orden.getFechaCreacion());

        detalle.put("estado", orden.getEstado());

        detalle.put("observaciones", orden.getObservaciones());

        detalle.put("externa", Boolean.TRUE.equals(orden.getOrdenExterna()));

        detalle.put("pagada", orden.getFechaPago() != null);

        detalle.put("montoTotal", orden.getMontoTotal());

        // EXÁMENES SOLICITADOS

        List<Map<String, Object>> examenes = new ArrayList<>();

        for (var ordenExamen : orden.getExamenes()) {

            Map<String, Object> examen = new LinkedHashMap<>();

            examen.put("id", ordenExamen.getId());

            examen.put(
                    "nombre",
                    ordenExamen.getExamen().getNombre()
            );

            examen.put(
                    "descripcion",
                    ordenExamen.getExamen().getDescripcion()
            );

            examen.put("precio", ordenExamen.getPrecio());

            examen.put(
                    "rangoReferencia",
                    ordenExamen.getExamen().getRangoReferencia()
            );

            examen.put(
                    "unidadReferencia",
                    ordenExamen.getExamen().getUnidadReferencia()
            );

            examen.put(
                    "valorResultado",
                    ordenExamen.getValorResultado()
            );

            examen.put("unidad", ordenExamen.getUnidad());

            examen.put(
                    "fueraRango",
                    Boolean.TRUE.equals(ordenExamen.getFueraRango())
            );

            examen.put(
                    "publicado",
                    Boolean.TRUE.equals(ordenExamen.getPublicado())
            );

            examenes.add(examen);
        }

        detalle.put("examenes", examenes);

        return detalle;
    }
}