package com.biblioteca.model;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla `prestamo`.
 */
public class Prestamo {
    private Integer id;
    private Integer socioId;
    private Integer libroId;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucion;
    private String estado;

    public Prestamo() {}

    public Prestamo(Integer id, Integer socioId, Integer libroId,
                    LocalDateTime fechaPrestamo, LocalDateTime fechaDevolucion, String estado) {
        this.id = id;
        this.socioId = socioId;
        this.libroId = libroId;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSocioId() { return socioId; }
    public void setSocioId(Integer socioId) { this.socioId = socioId; }

    public Integer getLibroId() { return libroId; }
    public void setLibroId(Integer libroId) { this.libroId = libroId; }

    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDateTime fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Prestamo{id=" + id + ", socioId=" + socioId + ", libroId=" + libroId
                + ", fechaPrestamo=" + fechaPrestamo + ", fechaDevolucion=" + fechaDevolucion
                + ", estado='" + estado + "'}";
    }
}
