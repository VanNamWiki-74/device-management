package com.devicemgmt.server.dao;

import com.devicemgmt.common.dto.CategoryDTO;
import com.devicemgmt.server.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private static final Logger log = LoggerFactory.getLogger(CategoryDAO.class);

    public List<CategoryDTO> findAll() {
        List<CategoryDTO> list = new ArrayList<>();
        String sql = """
            SELECT dc.*, COUNT(d.id) AS device_count
            FROM device_categories dc
            LEFT JOIN devices d ON d.category_id = dc.id
            GROUP BY dc.id ORDER BY dc.name
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll categories error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public boolean nameExists(String name, int excludeId) {
        String sql = "SELECT 1 FROM device_categories WHERE name ILIKE ? AND id != ?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name); ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            log.error("nameExists category error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return false;
    }

    public int insert(CategoryDTO c) {
        String sql = "INSERT INTO device_categories (name, description) VALUES (?, ?) RETURNING id";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("insert category error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return -1;
    }

    public boolean update(CategoryDTO c) {
        String sql = "UPDATE device_categories SET name=?, description=?, updated_at=NOW() WHERE id=?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update category error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean delete(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM device_categories WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete category error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    private CategoryDTO mapRow(ResultSet rs) throws SQLException {
        CategoryDTO c = new CategoryDTO();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setDeviceCount(rs.getInt("device_count"));
        return c;
    }
}
