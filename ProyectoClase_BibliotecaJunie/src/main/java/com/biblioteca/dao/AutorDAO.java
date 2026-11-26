package com.biblioteca.dao;

import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.model.Autor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Autor.
 */
public class AutorDAO {

    private DataSource dataSource;

    public AutorDAO() {
        this.dataSource = DataSourceProvider.getInstance();
    }

    public AutorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Autor create(Autor autor) throws DAOException {
        String sql = "INSERT INTO autor (nombre, nacionalidad) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getNacionalidad());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    autor.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al crear autor: " + e.getMessage(), e);
        }
        return autor;
    }

    public Autor findById(Integer id) throws DAOException {
        String sql = "SELECT id, nombre, nacionalidad FROM autor WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar autor por id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Autor> findAll() throws DAOException {
        String sql = "SELECT id, nombre, nacionalidad FROM autor ORDER BY nombre";
        List<Autor> autores = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                autores.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al listar autores: " + e.getMessage(), e);
        }
        return autores;
    }

    public Autor update(Autor autor) throws DAOException {
        String sql = "UPDATE autor SET nombre = ?, nacionalidad = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getNacionalidad());
            ps.setInt(3, autor.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Autor no encontrado con id: " + autor.getId());
            }
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar autor: " + e.getMessage(), e);
        }
        return autor;
    }

    public void delete(Integer id) throws DAOException {
        String sql = "DELETE FROM autor WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Autor no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar el autor porque tiene libros asociados.", e);
            }
            throw new DAOException("Error al borrar autor: " + e.getMessage(), e);
        }
    }

    private Autor mapResultSet(ResultSet rs) throws SQLException {
        Autor autor = new Autor();
        autor.setId(rs.getInt("id"));
        autor.setNombre(rs.getString("nombre"));
        autor.setNacionalidad(rs.getString("nacionalidad"));
        return autor;
    }

    // --- Métodos con Connection proporcionada (para transacciones) ---

    public Autor create(Connection conn, Autor autor) throws DAOException {
        String sql = "INSERT INTO autor (nombre, nacionalidad) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getNacionalidad());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    autor.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al crear autor: " + e.getMessage(), e);
        }
        return autor;
    }

    public Autor findById(Connection conn, Integer id) throws DAOException {
        String sql = "SELECT id, nombre, nacionalidad FROM autor WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar autor por id: " + e.getMessage(), e);
        }
        return null;
    }

    public Autor update(Connection conn, Autor autor) throws DAOException {
        String sql = "UPDATE autor SET nombre = ?, nacionalidad = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getNacionalidad());
            ps.setInt(3, autor.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Autor no encontrado con id: " + autor.getId());
            }
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar autor: " + e.getMessage(), e);
        }
        return autor;
    }

    public void delete(Connection conn, Integer id) throws DAOException {
        String sql = "DELETE FROM autor WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Autor no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar el autor porque tiene libros asociados.", e);
            }
            throw new DAOException("Error al borrar autor: " + e.getMessage(), e);
        }
    }
}
