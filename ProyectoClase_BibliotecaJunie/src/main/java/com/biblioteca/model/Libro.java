package com.biblioteca.model;

/**
 * Entidad que mapea la tabla `libro`.
 */
public class Libro {
    private Integer id;
    private String titulo;
    private String isbn;
    private int anioPublicacion;
    private int stock;
    private Integer autorId;

    public Libro() {}

    public Libro(Integer id, String titulo, String isbn, int anioPublicacion, int stock, Integer autorId) {
        this.id = id;
        this.titulo = titulo;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
        this.stock = stock;
        this.autorId = autorId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(int anioPublicacion) { this.anioPublicacion = anioPublicacion; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public Integer getAutorId() { return autorId; }
    public void setAutorId(Integer autorId) { this.autorId = autorId; }

    @Override
    public String toString() {
        return "Libro{id=" + id + ", titulo='" + titulo + "', isbn='" + isbn + "', anio=" + anioPublicacion + ", stock=" + stock + ", autorId=" + autorId + "}";
    }
}
