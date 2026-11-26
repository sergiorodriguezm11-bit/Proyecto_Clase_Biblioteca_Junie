package com.biblioteca.model;

import java.time.LocalDate;

/**
 * Entidad que mapea la tabla `socio`.
 */
public class Socio {
    private Integer id;
    private String nombre;
    private String email;
    private LocalDate fechaAlta;

    public Socio() {}

    public Socio(Integer id, String nombre, String email, LocalDate fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.fechaAlta = fechaAlta;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    @Override
    public String toString() {
        return "Socio{id=" + id + ", nombre='" + nombre + "', email='" + email + "', fechaAlta=" + fechaAlta + "}";
    }
}
