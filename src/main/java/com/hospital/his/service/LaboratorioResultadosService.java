package com.hospital.his.service;

import com.hospital.his.entity.OrdenExamen;
import com.hospital.his.entity.OrdenLaboratorio;
import com.hospital.his.repository.OrdenLaboratorioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LaboratorioResultadosService {

    private final OrdenLaboratorioRepository ordenRepository;

    public LaboratorioResultadosService(
            OrdenLaboratorioRepository ordenRepository) {

        this.ordenRepository = ordenRepository;
    }

    // ==========================================
    // CU-09: REGISTRAR RESULTADO DE LABORATORIO
    // ==========================================

    @Transactional
    public void registrarResultado(
            Long ordenId,
            Long examenId,
            String valorResultado,
            String unidad,
            String notasResultado,
            boolean fueraRango) {

        // ======================================
        // 1. BUSCAR ORDEN
        // ======================================

        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La orden de laboratorio no existe."
                        )
                );

        // ======================================
        // 2. VERIFICAR PAGO
        // ======================================

        boolean ordenExterna =
                Boolean.TRUE.equals(orden.getOrdenExterna());

        boolean pagoConfirmado =
                orden.getFechaPago() != null;

        if (!ordenExterna && !pagoConfirmado) {

            throw new IllegalStateException(
                    "No se pueden registrar resultados. "
                            + "La orden tiene el pago pendiente."
            );
        }

        // ======================================
        // 3. VALIDAR ESTADO DE LA ORDEN
        // ======================================

        if ("COMPLETADA".equalsIgnoreCase(orden.getEstado())) {

            throw new IllegalStateException(
                    "No se pueden modificar resultados "
                            + "de una orden completada."
            );
        }

        // ======================================
        // 4. BUSCAR EXAMEN EN LA ORDEN
        // ======================================

        OrdenExamen examen = orden.getExamenes()
                .stream()
                .filter(detalle ->
                        examenId.equals(detalle.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El examen no pertenece a esta orden."
                        )
                );

        // ======================================
        // 5. VALIDAR PUBLICACIÓN
        // ======================================

        if (Boolean.TRUE.equals(examen.getPublicado())) {

            throw new IllegalStateException(
                    "El resultado ya fue publicado "
                            + "y no puede modificarse."
            );
        }

        // ======================================
        // 6. VALIDAR RESULTADO
        // ======================================

        if (valorResultado == null
                || valorResultado.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe ingresar el resultado del examen."
            );
        }

        // ======================================
        // 7. GUARDAR RESULTADO
        // ======================================

        examen.setValorResultado(valorResultado.trim());

        examen.setUnidad(
                unidad == null ? "" : unidad.trim()
        );

        examen.setNotasResultado(
                notasResultado == null
                        ? ""
                        : notasResultado.trim()
        );

        examen.setFueraRango(fueraRango);

        examen.setFechaResultado(LocalDateTime.now());

        // ======================================
        // 8. ACTUALIZAR ESTADO DE LA ORDEN
        // ======================================

        orden.setEstado("EN_PROCESO");

        // ======================================
        // 9. GUARDAR CAMBIOS
        // ======================================

        ordenRepository.save(orden);
    }

    // ==========================================
    // CU-09: COMPLETAR ORDEN DE LABORATORIO
    // ==========================================

    @Transactional
    public void completarOrden(Long ordenId) {

        // ======================================
        // 1. VALIDAR IDENTIFICADOR
        // ======================================

        if (ordenId == null) {

            throw new IllegalArgumentException(
                    "Debe indicar una orden de laboratorio."
            );
        }

        // ======================================
        // 2. BUSCAR ORDEN
        // ======================================

        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La orden de laboratorio no existe."
                        )
                );

        // ======================================
        // 3. VERIFICAR SI YA ESTÁ COMPLETADA
        // ======================================

        if ("COMPLETADA".equalsIgnoreCase(orden.getEstado())) {

            throw new IllegalStateException(
                    "Esta orden de laboratorio ya está completada."
            );
        }

        // ======================================
        // 4. VERIFICAR CONDICIÓN DE PAGO
        // ======================================

        boolean ordenExterna =
                Boolean.TRUE.equals(orden.getOrdenExterna());

        boolean pagoConfirmado =
                orden.getFechaPago() != null;

        if (!ordenExterna && !pagoConfirmado) {

            throw new IllegalStateException(
                    "No se puede completar la orden "
                            + "porque el pago está pendiente."
            );
        }

        // ======================================
        // 5. OBTENER EXÁMENES
        // ======================================

        List<OrdenExamen> examenes = orden.getExamenes();

        if (examenes == null || examenes.isEmpty()) {

            throw new IllegalStateException(
                    "No se puede completar una orden "
                            + "que no contiene exámenes."
            );
        }

        // ======================================
        // 6. VALIDAR RESULTADOS PENDIENTES
        // ======================================

        long resultadosPendientes = examenes.stream()
                .filter(examen ->
                        examen.getValorResultado() == null
                                || examen.getValorResultado()
                                .trim()
                                .isEmpty()
                )
                .count();

        if (resultadosPendientes > 0) {

            throw new IllegalStateException(
                    "No se puede completar la orden. "
                            + "Existen "
                            + resultadosPendientes
                            + " examen(es) sin resultado."
            );
        }

        // ======================================
        // 7. COMPLETAR ORDEN
        // ======================================

        orden.setEstado("COMPLETADA");

        orden.setFechaCompletada(LocalDateTime.now());

        // ======================================
        // 8. GUARDAR CAMBIOS
        // ======================================

        ordenRepository.save(orden);
    }

    // ==========================================
    // CU-09: PUBLICAR RESULTADOS DE LABORATORIO
    // ==========================================

    @Transactional
    public void publicarResultados(Long ordenId) {

        // ======================================
        // 1. VALIDAR IDENTIFICADOR
        // ======================================

        if (ordenId == null) {

            throw new IllegalArgumentException(
                    "Debe indicar una orden de laboratorio."
            );
        }

        // ======================================
        // 2. BUSCAR ORDEN
        // ======================================

        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La orden de laboratorio no existe."
                        )
                );

        // ======================================
        // 3. VERIFICAR QUE ESTÉ COMPLETADA
        // ======================================

        if (!"COMPLETADA".equalsIgnoreCase(orden.getEstado())) {

            throw new IllegalStateException(
                    "No se pueden publicar los resultados. "
                            + "Primero debe completar la orden."
            );
        }

        // ======================================
        // 4. VERIFICAR EXÁMENES
        // ======================================

        List<OrdenExamen> examenes = orden.getExamenes();

        if (examenes == null || examenes.isEmpty()) {

            throw new IllegalStateException(
                    "No se pueden publicar resultados "
                            + "de una orden sin exámenes."
            );
        }

        // ======================================
        // 5. VALIDAR QUE TODOS TENGAN RESULTADO
        // ======================================

        long resultadosPendientes = examenes.stream()
                .filter(examen ->
                        examen.getValorResultado() == null
                                || examen.getValorResultado()
                                .trim()
                                .isEmpty()
                )
                .count();

        if (resultadosPendientes > 0) {

            throw new IllegalStateException(
                    "No se pueden publicar los resultados. "
                            + "Existen "
                            + resultadosPendientes
                            + " examen(es) sin resultado."
            );
        }

        // ======================================
        // 6. VERIFICAR SI YA ESTÁN PUBLICADOS
        // ======================================

        boolean todosPublicados = examenes.stream()
                .allMatch(examen ->
                        Boolean.TRUE.equals(examen.getPublicado())
                );

        if (todosPublicados) {

            throw new IllegalStateException(
                    "Todos los resultados de esta orden "
                            + "ya fueron publicados."
            );
        }

        // ======================================
        // 7. REGISTRAR FECHA DE PUBLICACIÓN
        // ======================================

        LocalDateTime fechaPublicacion = LocalDateTime.now();

        // ======================================
        // 8. PUBLICAR RESULTADOS PENDIENTES
        // ======================================

        for (OrdenExamen examen : examenes) {

            if (!Boolean.TRUE.equals(examen.getPublicado())) {

                examen.setPublicado(true);

                examen.setFechaPublicacion(fechaPublicacion);
            }
        }

        // ======================================
        // 9. GUARDAR CAMBIOS
        // ======================================

        ordenRepository.save(orden);
    }
}