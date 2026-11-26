package com.biblioteca.mapper;

import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.model.Categoria;

/**
 * Mapper entre Categoria (entidad) y CategoriaDTO.
 */
public class CategoriaMapper {

    public static CategoriaDTO toDTO(Categoria c) {
        if (c == null) return null;
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        return dto;
    }

    public static Categoria toEntity(CategoriaDTO dto) {
        if (dto == null) return null;
        Categoria c = new Categoria();
        c.setId(dto.getId());
        c.setNombre(dto.getNombre());
        return c;
    }
}
