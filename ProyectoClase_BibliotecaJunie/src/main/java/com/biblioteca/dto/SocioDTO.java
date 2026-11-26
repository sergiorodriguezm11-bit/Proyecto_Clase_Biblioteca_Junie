package com.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * DTO de respuesta para Socio.
 */
public class SocioDTO {
    private Integer id;
    private String nombre;
    private String email;

    @JsonProperty("fechaAlta")
    private LocalDate fechaAlta;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonProperty("fechaAlta")
    public LocalDate getFechaAlta() { return fechaAlta; }
    @JsonProperty("fechaAlta")
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }
}
