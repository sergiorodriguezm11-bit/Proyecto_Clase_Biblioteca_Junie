package com.biblioteca.dao;

import com.biblioteca.config.DataSourceProvider;
import com.biblioteca.model.Socio;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Socio.
 */
public class SocioDAO {

    private DataSource dataSource;

    public SocioDAO() {
        this.dataSource = DataSourceProvider.getInstance();
    }

    public SocioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Socio create(Socio socio) throws DAOException {
        String sql = "INSERT INTO socio (nombre, email, fecha_alta) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getEmail());
            ps.setDate(3, Date.valueOf(socio.getFechaAlta()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) socio.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El email ya está registrado: " + socio.getEmail(), e);
            }
            throw new DAOException("Error al crear socio: " + e.getMessage(), e);
        }
        return socio;
    }

    public Socio findById(Integer id) throws DAOException {
        String sql = "SELECT id, nombre, email, fecha_alta FROM socio WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar socio: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Socio> findAll() throws DAOException {
        String sql = "SELECT id, nombre, email, fecha_alta FROM socio ORDER BY nombre";
        List<Socio> socios = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) socios.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DAOException("Error al listar socios: " + e.getMessage(), e);
        }
        return socios;
    }

    public Socio update(Socio socio) throws DAOException {
        String sql = "UPDATE socio SET nombre = ?, email = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getEmail());
            ps.setInt(3, socio.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Socio no encontrado con id: " + socio.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El email ya está registrado: " + socio.getEmail(), e);
            }
            throw new DAOException("Error al actualizar socio: " + e.getMessage(), e);
        }
        return socio;
    }

    public void delete(Integer id) throws DAOException {
        String sql = "DELETE FROM socio WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Socio no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar el socio porque tiene préstamos registrados.", e);
            }
            throw new DAOException("Error al borrar socio: " + e.getMessage(), e);
        }
    }

    private Socio mapResultSet(ResultSet rs) throws SQLException {
        Socio socio = new Socio();
        socio.setId(rs.getInt("id"));
        socio.setNombre(rs.getString("nombre"));
        socio.setEmail(rs.getString("email"));
        socio.setFechaAlta(rs.getDate("fecha_alta").toLocalDate());
        return socio;
    }

    // --- Métodos con Connection (transacciones) ---

    public Socio create(Connection conn, Socio socio) throws DAOException {
        String sql = "INSERT INTO socio (nombre, email, fecha_alta) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getEmail());
            ps.setDate(3, Date.valueOf(socio.getFechaAlta()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) socio.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El email ya está registrado: " + socio.getEmail(), e);
            }
            throw new DAOException("Error al crear socio: " + e.getMessage(), e);
        }
        return socio;
    }

    public Socio findById(Connection conn, Integer id) throws DAOException {
        String sql = "SELECT id, nombre, email, fecha_alta FROM socio WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar socio: " + e.getMessage(), e);
        }
        return null;
    }

    public Socio update(Connection conn, Socio socio) throws DAOException {
        String sql = "UPDATE socio SET nombre = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getEmail());
            ps.setInt(3, socio.getId());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Socio no encontrado con id: " + socio.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                throw new DAOException("El email ya está registrado: " + socio.getEmail(), e);
            }
            throw new DAOException("Error al actualizar socio: " + e.getMessage(), e);
        }
        return socio;
    }

    public void delete(Connection conn, Integer id) throws DAOException {
        String sql = "DELETE FROM socio WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Socio no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key")) {
                throw new DAOException("No se puede borrar el socio porque tiene préstamos registrados.", e);
            }
            throw new DAOException("Error al borrar socio: " + e.getMessage(), e);
        }
    }
}
