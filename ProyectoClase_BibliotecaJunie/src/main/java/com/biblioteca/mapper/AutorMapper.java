package com.biblioteca.mapper;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.model.Autor;

/**
 * Mapper entre Autor (entidad) y AutorDTO.
 */
public class AutorMapper {

    public static AutorDTO toDTO(Autor autor) {
        if (autor == null) return null;
        AutorDTO dto = new AutorDTO();
        dto.setId(autor.getId());
        dto.setNombre(autor.getNombre());
        dto.setNacionalidad(autor.getNacionalidad());
        return dto;
    }

    public static Autor toEntity(AutorDTO dto) {
        if (dto == null) return null;
        Autor autor = new Autor();
        autor.setId(dto.getId());
        autor.setNombre(dto.getNombre());
        autor.setNacionalidad(dto.getNacionalidad());
        return autor;
    }
}
