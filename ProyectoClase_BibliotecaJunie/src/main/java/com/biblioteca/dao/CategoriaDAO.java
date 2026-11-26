package com.biblioteca.dao;

import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.model.Categoria;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Categoria.
 */
public class CategoriaDAO {

    private DataSource dataSource;

    public CategoriaDAO() {
        this.dataSource = DataSourceProvider.getInstance();
    }

    public CategoriaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Categoria create(Categoria categoria) throws DAOException {
        String sql = "INSERT INTO categoria (nombre) VALUES (?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) categoria.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("La categoría ya existe: " + categoria.getNombre(), e);
            }
            throw new DAOException("Error al crear categoría: " + e.getMessage(), e);
        }
        return categoria;
    }

    public Categoria findById(Integer id) throws DAOException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar categoría: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Categoria> findAll() throws DAOException {
        String sql = "SELECT id, nombre FROM categoria ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) categorias.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DAOException("Error al listar categorías: " + e.getMessage(), e);
        }
        return categorias;
    }

    public Categoria update(Categoria categoria) throws DAOException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Categoría no encontrada con id: " + categoria.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("Ya existe otra categoría con ese nombre", e);
            }
            throw new DAOException("Error al actualizar categoría: " + e.getMessage(), e);
        }
        return categoria;
    }

    public void delete(Integer id) throws DAOException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Categoría no encontrada con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar la categoría porque tiene libros asociados.", e);
            }
            throw new DAOException("Error al borrar categoría: " + e.getMessage(), e);
        }
    }

    private Categoria mapResultSet(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id"), rs.getString("nombre"));
    }

    // --- Métodos con Connection (transacciones) ---

    public Categoria create(Connection conn, Categoria categoria) throws DAOException {
        String sql = "INSERT INTO categoria (nombre) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) categoria.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("La categoría ya existe: " + categoria.getNombre(), e);
            }
            throw new DAOException("Error al crear categoría: " + e.getMessage(), e);
        }
        return categoria;
    }

    public Categoria findById(Connection conn, Integer id) throws DAOException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar categoría: " + e.getMessage(), e);
        }
        return null;
    }

    public Categoria update(Connection conn, Categoria categoria) throws DAOException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Categoría no encontrada con id: " + categoria.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("Ya existe otra categoría con ese nombre", e);
            }
            throw new DAOException("Error al actualizar categoría: " + e.getMessage(), e);
        }
        return categoria;
    }

    public void delete(Connection conn, Integer id) throws DAOException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Categoría no encontrada con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar la categoría porque tiene libros asociados.", e);
            }
            throw new DAOException("Error al borrar categoría: " + e.getMessage(), e);
        }
    }
}
