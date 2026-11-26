package com.biblioteca.mapper;

import com.biblioteca.dto.SocioDTO;
import com.biblioteca.model.Socio;

/**
 * Mapper entre Socio (entidad) y SocioDTO.
 */
public class SocioMapper {

    public static SocioDTO toDTO(Socio socio) {
        if (socio == null) return null;
        SocioDTO dto = new SocioDTO();
        dto.setId(socio.getId());
        dto.setNombre(socio.getNombre());
        dto.setEmail(socio.getEmail());
        dto.setFechaAlta(socio.getFechaAlta());
        return dto;
    }

    public static Socio toEntity(SocioDTO dto) {
        if (dto == null) return null;
        Socio socio = new Socio();
        socio.setId(dto.getId());
        socio.setNombre(dto.getNombre());
        socio.setEmail(dto.getEmail());
        socio.setFechaAlta(dto.getFechaAlta());
        return socio;
    }
}
