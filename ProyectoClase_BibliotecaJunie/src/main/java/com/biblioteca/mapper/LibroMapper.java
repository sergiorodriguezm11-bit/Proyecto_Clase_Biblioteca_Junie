package com.biblioteca.mapper;

import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.DAOException;
import com.biblioteca.dao.LibroCategoriaDAO;
import com.biblioteca.dto.AutorDTO;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.dto.CrearLibroRequest;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroCategoria;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper entre Libro (entidad) y LibroDTO.
 */
public class LibroMapper {

    public static LibroDTO toDTO(Libro libro, AutorDTO autor, List<CategoriaDTO> categorias) {
        if (libro == null) return null;
        LibroDTO dto = new LibroDTO();
        dto.setId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setIsbn(libro.getIsbn());
        dto.setAnioPublicacion(libro.getAnioPublicacion());
        dto.setStock(libro.getStock());
        dto.setAutor(autor);
        dto.setCategorias(categorias != null ? categorias : new ArrayList<>());
        return dto;
    }

    public static Libro toEntity(CrearLibroRequest req) {
        if (req == null) return null;
        Libro libro = new Libro();
        libro.setTitulo(req.getTitulo());
        libro.setIsbn(req.getIsbn());
        libro.setAnioPublicacion(req.getAnioPublicacion());
        libro.setStock(req.getStock());
        libro.setAutorId(req.getAutorId());
        return libro;
    }

    public static List<CategoriaDTO> mapCategorias(List<LibroCategoria> libroCategorias, CategoriaDAO catDAO) throws DAOException {
        List<CategoriaDTO> dtos = new ArrayList<>();
        for (LibroCategoria lc : libroCategorias) {
            var cat = catDAO.findById(lc.getCategoriaId());
            if (cat != null) {
                dtos.add(CategoriaMapper.toDTO(cat));
            }
        }
        return dtos;
    }
}
