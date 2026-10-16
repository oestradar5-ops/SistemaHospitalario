package com.hospital.his.dto;

public class FarmaciaSustitucionRequest {

    private Long recetaMedicamentoId;

    private Long medicamentoSustitutoId;

    private String motivo;

    public FarmaciaSustitucionRequest() {
    }

    public Long getRecetaMedicamentoId() {
        return recetaMedicamentoId;
    }

    public void setRecetaMedicamentoId(
            Long recetaMedicamentoId) {
        this.recetaMedicamentoId = recetaMedicamentoId;
    }

    public Long getMedicamentoSustitutoId() {
        return medicamentoSustitutoId;
    }

    public void setMedicamentoSustitutoId(
            Long medicamentoSustitutoId) {
        this.medicamentoSustitutoId = medicamentoSustitutoId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}