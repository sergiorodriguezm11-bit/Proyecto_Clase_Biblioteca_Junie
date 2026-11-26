package com.biblioteca.service;

import com.biblioteca.dao.AutorDAO;
import com.biblioteca.dao.DAOException;
import com.biblioteca.dto.AutorDTO;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ServicioException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.mapper.AutorMapper;
import com.biblioteca.model.Autor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la entidad Autor.
 */
public class AutorService {

    private final AutorDAO autorDAO;

    public AutorService() {
        this.autorDAO = new AutorDAO();
    }

    public AutorService(AutorDAO autorDAO) {
        this.autorDAO = autorDAO;
    }

    public List<AutorDTO> listar() throws ServicioException {
        try {
            return autorDAO.findAll().stream()
                    .map(AutorMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            throw new ServicioException("Error al listar autores", e);
        }
    }

    public AutorDTO buscarPorId(Integer id) throws RecursoNoEncontradoException, ServicioException {
        try {
            Autor autor = autorDAO.findById(id);
            if (autor == null) {
                throw new RecursoNoEncontradoException("Autor no encontrado con id: " + id);
            }
            return AutorMapper.toDTO(autor);
        } catch (DAOException e) {
            throw new ServicioException("Error al buscar autor", e);
        }
    }

    public AutorDTO crear(AutorDTO dto) throws ValidacionException, ConflictoException, ServicioException {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre es obligatorio.");
        }
        if (dto.getNacionalidad() == null || dto.getNacionalidad().trim().isEmpty()) {
            throw new ValidacionException("nacionalidad", "La nacionalidad es obligatoria.");
        }
        try {
            Autor autor = AutorMapper.toEntity(dto);
            autor = autorDAO.create(autor);
            return AutorMapper.toDTO(autor);
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new ConflictoException("Ya existe un autor con ese nombre.");
            }
            throw new ServicioException("Error al crear autor", e);
        }
    }

    public AutorDTO actualizar(Integer id, AutorDTO dto) throws RecursoNoEncontradoException, ValidacionException, ServicioException {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre es obligatorio.");
        }
        if (dto.getNacionalidad() == null || dto.getNacionalidad().trim().isEmpty()) {
            throw new ValidacionException("nacionalidad", "La nacionalidad es obligatoria.");
        }
        try {
            Autor existente = autorDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Autor no encontrado con id: " + id);
            }
            Autor autor = AutorMapper.toEntity(dto);
            autor.setId(id);
            autor = autorDAO.update(autor);
            return AutorMapper.toDTO(autor);
        } catch (DAOException e) {
            throw new ServicioException("Error al actualizar autor", e);
        }
    }

    public void eliminar(Integer id) throws RecursoNoEncontradoException, ConflictoException, ServicioException {
        try {
            Autor existente = autorDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Autor no encontrado con id: " + id);
            }
            autorDAO.delete(id);
        } catch (DAOException e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("No se puede borrar")) {
                throw new ConflictoException("No se puede borrar el autor porque tiene libros asociados.");
            }
            throw new ServicioException("Error al eliminar autor", e);
        }
    }
}
