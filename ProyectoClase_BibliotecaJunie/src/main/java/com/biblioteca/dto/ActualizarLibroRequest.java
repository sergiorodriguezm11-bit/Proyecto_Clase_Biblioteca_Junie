package com.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO de solicitud para actualizar un libro.
 */
public class ActualizarLibroRequest {
    private String titulo;
    private String isbn;

    @JsonProperty("anioPublicacion")
    private int anioPublicacion;

    private int stock;

    @JsonProperty("autorId")
    private int autorId;

    @JsonProperty("categoriaIds")
    private List<Integer> categoriaIds;

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
    @JsonProperty("autorId")
    public int getAutorId() { return autorId; }
    @JsonProperty("autorId")
    public void setAutorId(int autorId) { this.autorId = autorId; }
    @JsonProperty("categoriaIds")
    public List<Integer> getCategoriaIds() { return categoriaIds; }
    @JsonProperty("categoriaIds")
    public void setCategoriaIds(List<Integer> categoriaIds) { this.categoriaIds = categoriaIds; }
}
