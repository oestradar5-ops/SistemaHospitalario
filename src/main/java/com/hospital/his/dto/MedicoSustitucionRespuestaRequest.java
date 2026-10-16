package com.hospital.his.dto;

public class MedicoSustitucionRespuestaRequest {

    private String decision;

    private String observaciones;

    public MedicoSustitucionRespuestaRequest() {
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}