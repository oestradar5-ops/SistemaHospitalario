package com.hospital.his.service;

import com.hospital.his.entity.RecetaMedica;
import com.hospital.his.entity.RechazoMedicamento;
import com.hospital.his.entity.Sucursal;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.RecetaMedicaRepository;
import com.hospital.his.repository.RechazoMedicamentoRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FarmaciaRechazoService {

    // ==========================================
    // SERVICIOS
    // ==========================================

    private final FarmaciaSesionService farmaciaSesionService;

    private final FarmaciaRecetaService farmaciaRecetaService;

    // ==========================================
    // REPOSITORIOS
    // ==========================================

    private final RecetaMedicaRepository recetaMedicaRepository;

    private final RechazoMedicamentoRepository rechazoRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaRechazoService(
            FarmaciaSesionService farmaciaSesionService,
            FarmaciaRecetaService farmaciaRecetaService,
            RecetaMedicaRepository recetaMedicaRepository,
            RechazoMedicamentoRepository rechazoRepository) {

        this.farmaciaSesionService = farmaciaSesionService;
        this.farmaciaRecetaService = farmaciaRecetaService;
        this.recetaMedicaRepository = recetaMedicaRepository;
        this.rechazoRepository = rechazoRepository;
    }

    // ==========================================
    // CU-11: REGISTRAR RECHAZO DE MEDICAMENTOS
    // ==========================================

    @Transactional
    public RechazoMedicamento registrarRechazo(
            Long recetaId,
            String motivo,
            HttpSession sesion) {

        // ======================================
        // 1. VALIDAR SESIÓN DE FARMACIA
        // ======================================

        Usuario empleado =
                farmaciaSesionService.obtenerUsuario(sesion);

        Sucursal sucursal =
                farmaciaSesionService.obtenerSucursal(sesion);

        // ======================================
        // 2. VALIDAR DATOS
        // ======================================

        if (recetaId == null || recetaId <= 0) {

            throw new IllegalArgumentException(
                    "Debe indicar una receta médica válida."
            );
        }

        if (motivo == null || motivo.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar el motivo del rechazo."
            );
        }

        String motivoLimpio = motivo.trim();

        // ======================================
        // 3. BLOQUEAR RECETA
        // ======================================

        RecetaMedica receta = recetaMedicaRepository
                .bloquearRecetaParaDespacho(recetaId)
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
        // 5. EVITAR RECHAZOS DUPLICADOS
        // ======================================

        boolean rechazoExistente =
                rechazoRepository.existsByRecetaIdAndEstado(
                        recetaId,
                        "RECHAZADO"
                );

        if (rechazoExistente) {

            throw new IllegalStateException(
                    "Esta receta ya tiene un rechazo registrado."
            );
        }

        // ======================================
        // 6. CREAR RECHAZO
        // ======================================

        RechazoMedicamento rechazo =
                new RechazoMedicamento();

        rechazo.setReceta(receta);

        rechazo.setSucursal(sucursal);

        rechazo.setUsuario(empleado);

        rechazo.setMotivo(motivoLimpio);

        rechazo.setEstado("RECHAZADO");

        // ======================================
        // 7. GUARDAR EN POSTGRESQL
        // ======================================

        return rechazoRepository.save(rechazo);
    }
}