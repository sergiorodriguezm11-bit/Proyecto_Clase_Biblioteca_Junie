package com.biblioteca.dao;

import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.model.LibroCategoria;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de unión libro_categoria.
 */
public class LibroCategoriaDAO {

    private DataSource dataSource;

    public LibroCategoriaDAO() {
        this.dataSource = DataSourceProvider.getInstance();
    }

    public LibroCategoriaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Integer libroId, Integer categoriaId) throws DAOException {
        insert(null, libroId, categoriaId);
    }

    public void insert(Connection conn, Integer libroId, Integer categoriaId) throws DAOException {
        String sql = "INSERT INTO libro_categoria (libro_id, categoria_id) VALUES (?, ?)";
        try (Connection connection = conn == null ? dataSource.getConnection() : null;
             PreparedStatement ps = (conn != null ? conn : connection).prepareStatement(sql)) {
            ps.setInt(1, libroId);
            ps.setInt(2, categoriaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al insertar libro_categoria: " + e.getMessage(), e);
        }
    }

    public void deleteByLibroId(Integer libroId) throws DAOException {
        deleteByLibroId(null, libroId);
    }

    public void deleteByLibroId(Connection conn, Integer libroId) throws DAOException {
        String sql = "DELETE FROM libro_categoria WHERE libro_id = ?";
        try (Connection connection = conn == null ? dataSource.getConnection() : null;
             PreparedStatement ps = (conn != null ? conn : connection).prepareStatement(sql)) {
            ps.setInt(1, libroId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar categorias del libro: " + e.getMessage(), e);
        }
    }

    public List<LibroCategoria> findByLibroId(Integer libroId) throws DAOException {
        String sql = "SELECT libro_id, categoria_id FROM libro_categoria WHERE libro_id = ?";
        List<LibroCategoria> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new LibroCategoria(rs.getInt("libro_id"), rs.getInt("categoria_id")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar categorias del libro: " + e.getMessage(), e);
        }
        return results;
    }

    public List<LibroCategoria> findByCategoriaId(Integer categoriaId) throws DAOException {
        String sql = "SELECT libro_id, categoria_id FROM libro_categoria WHERE categoria_id = ?";
        List<LibroCategoria> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new LibroCategoria(rs.getInt("libro_id"), rs.getInt("categoria_id")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar libros de la categoria: " + e.getMessage(), e);
        }
        return results;
    }
}
