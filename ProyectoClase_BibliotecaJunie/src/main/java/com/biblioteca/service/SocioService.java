package com.biblioteca.service;

import com.biblioteca.dao.DAOException;
import com.biblioteca.dao.SocioDAO;
import com.biblioteca.dto.SocioDTO;
import com.biblioteca.exception.*;
import com.biblioteca.mapper.SocioMapper;
import com.biblioteca.model.Socio;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la entidad Socio.
 */
public class SocioService {

    private final SocioDAO socioDAO;

    public SocioService() {
        this.socioDAO = new SocioDAO();
    }

    public SocioService(SocioDAO socioDAO) {
        this.socioDAO = socioDAO;
    }

    public List<SocioDTO> listar() throws ServicioException {
        try {
            return socioDAO.findAll().stream()
                    .map(SocioMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            throw new ServicioException("Error al listar socios", e);
        }
    }

    public SocioDTO buscarPorId(Integer id) throws RecursoNoEncontradoException, ServicioException {
        try {
            Socio socio = socioDAO.findById(id);
            if (socio == null) {
                throw new RecursoNoEncontradoException("Socio no encontrado con id: " + id);
            }
            return SocioMapper.toDTO(socio);
        } catch (DAOException e) {
            throw new ServicioException("Error al buscar socio", e);
        }
    }

    public SocioDTO crear(SocioDTO dto) throws ValidacionException, ConflictoException, ServicioException {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre es obligatorio.");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new ValidacionException("email", "El email es obligatorio.");
        }
        if (!dto.getEmail().contains("@")) {
            throw new ValidacionException("email", "El email no tiene un formato válido.");
        }
        try {
            Socio socio = SocioMapper.toEntity(dto);
            socio.setFechaAlta(LocalDate.now());
            socio = socioDAO.create(socio);
            return SocioMapper.toDTO(socio);
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("email")) {
                throw new ConflictoException("El email ya está registrado: " + dto.getEmail());
            }
            throw new ServicioException("Error al crear socio", e);
        }
    }

    public SocioDTO actualizar(Integer id, SocioDTO dto) throws RecursoNoEncontradoException,
            ValidacionException, ConflictoException, ServicioException {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre es obligatorio.");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new ValidacionException("email", "El email es obligatorio.");
        }
        if (!dto.getEmail().contains("@")) {
            throw new ValidacionException("email", "El email no tiene un formato válido.");
        }
        try {
            Socio existente = socioDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Socio no encontrado con id: " + id);
            }
            Socio socio = SocioMapper.toEntity(dto);
            socio.setId(id);
            socio = socioDAO.update(socio);
            return SocioMapper.toDTO(socio);
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("email")) {
                throw new ConflictoException("El email ya está registrado: " + dto.getEmail());
            }
            throw new ServicioException("Error al actualizar socio", e);
        }
    }

    public void eliminar(Integer id) throws RecursoNoEncontradoException, ConflictoException, ServicioException {
        try {
            Socio existente = socioDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Socio no encontrado con id: " + id);
            }
            socioDAO.delete(id);
        } catch (DAOException e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("No se puede borrar")) {
                throw new ConflictoException("No se puede borrar el socio porque tiene préstamos registrados.");
            }
            throw new ServicioException("Error al eliminar socio", e);
        }
    }
}
