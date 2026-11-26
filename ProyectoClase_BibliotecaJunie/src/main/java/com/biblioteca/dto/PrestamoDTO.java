package com.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Prestamo (incluye socio y libro anidados).
 */
public class PrestamoDTO {
    private Integer id;
    private SocioDTO socio;
    private LibroDTO libro;

    @JsonProperty("fechaPrestamo")
    private LocalDateTime fechaPrestamo;

    @JsonProperty("fechaDevolucion")
    private LocalDateTime fechaDevolucion;

    private String estado;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public SocioDTO getSocio() { return socio; }
    public void setSocio(SocioDTO socio) { this.socio = socio; }
    public LibroDTO getLibro() { return libro; }
    public void setLibro(LibroDTO libro) { this.libro = libro; }
    @JsonProperty("fechaPrestamo")
    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    @JsonProperty("fechaPrestamo")
    public void setFechaPrestamo(LocalDateTime fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    @JsonProperty("fechaDevolucion")
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    @JsonProperty("fechaDevolucion")
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
