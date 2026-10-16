package com.hospital.his.dto;

import java.util.ArrayList;
import java.util.List;

public class FarmaciaDespachoRequest {

    // ==========================================
    // IDENTIFICADOR DE LA RECETA
    // ==========================================

    private Long recetaId;

    // ==========================================
    // MEDICAMENTOS A DESPACHAR
    // ==========================================

    private List<FarmaciaDetalleRequest> detalles =
            new ArrayList<>();

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FarmaciaDespachoRequest() {
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getRecetaId() {
        return recetaId;
    }

    public void setRecetaId(Long recetaId) {
        this.recetaId = recetaId;
    }

    public List<FarmaciaDetalleRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(
            List<FarmaciaDetalleRequest> detalles) {

        this.detalles = detalles;
    }
}