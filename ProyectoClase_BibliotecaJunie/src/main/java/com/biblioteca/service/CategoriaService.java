package com.biblioteca.service;

import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.DAOException;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ServicioException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.mapper.CategoriaMapper;
import com.biblioteca.model.Categoria;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la entidad Categoria.
 */
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaService() {
        this.categoriaDAO = new CategoriaDAO();
    }

    public CategoriaService(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public List<CategoriaDTO> listar() throws ServicioException {
        try {
            return categoriaDAO.findAll().stream()
                    .map(CategoriaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            throw new ServicioException("Error al listar categorías", e);
        }
    }

    public CategoriaDTO buscarPorId(Integer id) throws RecursoNoEncontradoException, ServicioException {
        try {
            Categoria cat = categoriaDAO.findById(id);
            if (cat == null) {
                throw new RecursoNoEncontradoException("Categoría no encontrada con id: " + id);
            }
            return CategoriaMapper.toDTO(cat);
        } catch (DAOException e) {
            throw new ServicioException("Error al buscar categoría", e);
        }
    }

    public CategoriaDTO crear(CategoriaDTO dto) throws ValidacionException, ConflictoException, ServicioException {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre es obligatorio.");
        }
        try {
            Categoria cat = CategoriaMapper.toEntity(dto);
            cat = categoriaDAO.create(cat);
            return CategoriaMapper.toDTO(cat);
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("ya existe")) {
                throw new ConflictoException("Ya existe una categoría con ese nombre.");
            }
            throw new ServicioException("Error al crear categoría", e);
        }
    }

    public CategoriaDTO actualizar(Integer id, CategoriaDTO dto) throws RecursoNoEncontradoException, ValidacionException, ConflictoException, ServicioException {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre es obligatorio.");
        }
        try {
            Categoria existente = categoriaDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Categoría no encontrada con id: " + id);
            }
            Categoria cat = CategoriaMapper.toEntity(dto);
            cat.setId(id);
            cat = categoriaDAO.update(cat);
            return CategoriaMapper.toDTO(cat);
        } catch (DAOException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new ConflictoException("Ya existe otra categoría con ese nombre.");
            }
            throw new ServicioException("Error al actualizar categoría", e);
        }
    }

    public void eliminar(Integer id) throws RecursoNoEncontradoException, ConflictoException, ServicioException {
        try {
            Categoria existente = categoriaDAO.findById(id);
            if (existente == null) {
                throw new RecursoNoEncontradoException("Categoría no encontrada con id: " + id);
            }
            categoriaDAO.delete(id);
        } catch (DAOException e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("No se puede borrar")) {
                throw new ConflictoException("No se puede borrar la categoría porque tiene libros asociados.");
            }
            throw new ServicioException("Error al eliminar categoría", e);
        }
    }
}
