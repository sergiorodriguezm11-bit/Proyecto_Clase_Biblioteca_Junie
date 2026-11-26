package com.biblioteca.mapper;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.dto.PrestamoDTO;
import com.biblioteca.dto.SocioDTO;
import com.biblioteca.model.Prestamo;

/**
 * Mapper entre Prestamo (entidad) y PrestamoDTO.
 */
public class PrestamoMapper {

    public static PrestamoDTO toDTO(Prestamo prestamo, SocioDTO socio, LibroDTO libro) {
        if (prestamo == null) return null;
        PrestamoDTO dto = new PrestamoDTO();
        dto.setId(prestamo.getId());
        dto.setSocio(socio);
        dto.setLibro(libro);
        dto.setFechaPrestamo(prestamo.getFechaPrestamo());
        dto.setFechaDevolucion(prestamo.getFechaDevolucion());
        dto.setEstado(prestamo.getEstado());
        return dto;
    }
}
