package com.hospital.his.service;

import com.hospital.his.entity.ConsultaMedica;
import com.hospital.his.entity.RecetaMedica;

import com.hospital.his.repository.ConsultaMedicaRepository;
import com.hospital.his.repository.RecetaMedicaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FarmaciaRecetaService {

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final RecetaMedicaRepository recetaMedicaRepository;

    private final ConsultaMedicaRepository consultaMedicaRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaRecetaService(
            RecetaMedicaRepository recetaMedicaRepository,
            ConsultaMedicaRepository consultaMedicaRepository) {

        this.recetaMedicaRepository = recetaMedicaRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
    }

    // ==========================================
    // BUSCAR RECETA POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public RecetaMedica buscarPorId(Long recetaId) {

        if (recetaId == null || recetaId <= 0) {
            throw new IllegalArgumentException(
                    "Debe ingresar un ID de receta válido."
            );
        }

        return recetaMedicaRepository
                .buscarRecetaConMedicamentos(recetaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró la receta médica."
                        )
                );
    }

    // ==========================================
    // BUSCAR RECETAS POR ID DE CONSULTA
    // ==========================================

    @Transactional(readOnly = true)
    public List<RecetaMedica> buscarPorConsulta(
            Long consultaId) {

        if (consultaId == null || consultaId <= 0) {
            throw new IllegalArgumentException(
                    "Debe ingresar un ID de consulta válido."
            );
        }

        ConsultaMedica consulta =
                consultaMedicaRepository.findById(consultaId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la consulta médica."
                                )
                        );

        Long citaId = consulta.getCita().getId();

        return recetaMedicaRepository
                .buscarPorCitaConMedicamentos(citaId);
    }

    // ==========================================
    // COMPROBAR VIGENCIA DE LA RECETA
    // ==========================================

    public boolean estaVigente(RecetaMedica receta) {

        if (receta == null
                || receta.getFechaCreacion() == null) {
            return false;
        }

        LocalDateTime fechaCreacion =
                receta.getFechaCreacion();

        LocalDateTime ahora = LocalDateTime.now();

        LocalDateTime fechaVencimiento =
                fechaCreacion.plusDays(7);

        return !fechaCreacion.isAfter(ahora)
                && !ahora.isAfter(fechaVencimiento);
    }

    // ==========================================
    // CU-11: VALIDAR RECETA PARA DESPACHO
    // PERMITE DESPACHOS NORMALES Y PARCIALES
    // ==========================================

    public void validarRecetaParaDespacho(
            RecetaMedica receta) {

        if (receta == null) {
            throw new IllegalArgumentException(
                    "La receta médica no existe."
            );
        }

        String estado = receta.getEstado();

        boolean pendiente =
                "PENDIENTE".equalsIgnoreCase(estado);

        boolean parcialmenteDespachada =
                "PARCIALMENTE_DESPACHADA"
                        .equalsIgnoreCase(estado);

        if (!pendiente && !parcialmenteDespachada) {

            throw new IllegalStateException(
                    "La receta no está disponible para despacho. "
                            + "Estado actual: " + estado
            );
        }

        if (!estaVigente(receta)) {

            throw new IllegalStateException(
                    "La receta médica está vencida. "
                            + "Su vigencia máxima es de 7 días."
            );
        }

        if (receta.getMedicamentos() == null
                || receta.getMedicamentos().isEmpty()) {

            throw new IllegalStateException(
                    "La receta no contiene medicamentos."
            );
        }
    }

    // ==========================================
    // OBTENER FECHA DE VENCIMIENTO
    // ==========================================

    public LocalDateTime obtenerFechaVencimiento(
            RecetaMedica receta) {

        if (receta == null
                || receta.getFechaCreacion() == null) {

            throw new IllegalArgumentException(
                    "La receta no tiene fecha de creación."
            );
        }

        return receta.getFechaCreacion().plusDays(7);
    }
}