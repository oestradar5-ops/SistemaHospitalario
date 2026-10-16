
package com.hospital.his.dto;

public class FarmaciaRechazoRequest {

    private Long recetaId;
    private String motivo;

    public FarmaciaRechazoRequest() {
    }

    public Long getRecetaId() {
        return recetaId;
    }

    public void setRecetaId(Long recetaId) {
        this.recetaId = recetaId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
