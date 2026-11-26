package com.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * DTO uniforme para respuestas de error.
 */
public class ErrorResponse {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("status")
    private int status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("detalles")
    private List<String> detalles;

    public ErrorResponse(int status, String error, String mensaje, List<String> detalles) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.detalles = detalles;
    }

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMensaje() { return mensaje; }
    public List<String> getDetalles() { return detalles; }
}
