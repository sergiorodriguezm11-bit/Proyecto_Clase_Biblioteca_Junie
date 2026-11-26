package com.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO de respuesta para Libro (incluye autor y categorías anidadas).
 */
public class LibroDTO {
    private Integer id;
    private String titulo;
    private String isbn;

    @JsonProperty("anioPublicacion")
    private int anioPublicacion;

    private int stock;
    private AutorDTO autor;
    private List<CategoriaDTO> categorias;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    @JsonProperty("anioPublicacion")
    public int getAnioPublicacion() { return anioPublicacion; }
    @JsonProperty("anioPublicacion")
    public void setAnioPublicacion(int v) { this.anioPublicacion = v; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public AutorDTO getAutor() { return autor; }
    public void setAutor(AutorDTO autor) { this.autor = autor; }
    public List<CategoriaDTO> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaDTO> categorias) { this.categorias = categorias; }
}
