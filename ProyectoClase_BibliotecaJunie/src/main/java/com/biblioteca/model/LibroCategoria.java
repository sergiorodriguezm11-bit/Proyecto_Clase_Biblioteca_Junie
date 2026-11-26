package com.biblioteca.model;

/**
 * Entidad que mapea la tabla `libro_categoria`.
 */
public class LibroCategoria {
    private Integer libroId;
    private Integer categoriaId;

    public LibroCategoria() {}

    public LibroCategoria(Integer libroId, Integer categoriaId) {
        this.libroId = libroId;
        this.categoriaId = categoriaId;
    }

    public Integer getLibroId() { return libroId; }
    public void setLibroId(Integer libroId) { this.libroId = libroId; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    @Override
    public String toString() {
        return "LibroCategoria{libroId=" + libroId + ", categoriaId=" + categoriaId + "}";
    }
}
