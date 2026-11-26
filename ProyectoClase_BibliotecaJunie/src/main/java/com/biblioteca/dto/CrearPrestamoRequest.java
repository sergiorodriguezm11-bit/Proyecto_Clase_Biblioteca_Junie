package com.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de solicitud para crear un préstamo.
 */
public class CrearPrestamoRequest {

    @JsonProperty("socioId")
    private int socioId;

    @JsonProperty("libroId")
    private int libroId;

    @JsonProperty("socioId")
    public int getSocioId() { return socioId; }
    @JsonProperty("socioId")
    public void setSocioId(int socioId) { this.socioId = socioId; }
    @JsonProperty("libroId")
    public int getLibroId() { return libroId; }
    @JsonProperty("libroId")
    public void setLibroId(int libroId) { this.libroId = libroId; }
}
