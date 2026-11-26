package com.biblioteca.mapper;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.dto.CrearLibroRequest;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.model.Libro;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LibroMapperTest {

    @Test
    void convierteSolicitudEnEntidad() {
        CrearLibroRequest request = new CrearLibroRequest();
        request.setTitulo("Libro");
        request.setIsbn("9780000000001");
        request.setAnioPublicacion(2020);
        request.setStock(4);
        request.setAutorId(9);

        Libro libro = LibroMapper.toEntity(request);

        assertEquals("Libro", libro.getTitulo());
        assertEquals("9780000000001", libro.getIsbn());
        assertEquals(2020, libro.getAnioPublicacion());
        assertEquals(4, libro.getStock());
        assertEquals(9, libro.getAutorId());
    }

    @Test
    void convierteEntidadEnDtoConAutorYCategorias() {
        Libro libro = new Libro(2, "Libro", "9780000000001", 2020, 4, 9);
        AutorDTO autor = new AutorDTO();
        autor.setId(9);
        CategoriaDTO categoria = new CategoriaDTO();
        categoria.setId(5);

        LibroDTO dto = LibroMapper.toDTO(libro, autor, List.of(categoria));

        assertEquals(2, dto.getId());
        assertEquals(autor, dto.getAutor());
        assertEquals(List.of(categoria), dto.getCategorias());
        assertNull(LibroMapper.toDTO(null, null, null));
        assertNotNull(LibroMapper.toDTO(libro, null, null).getCategorias());
    }
}