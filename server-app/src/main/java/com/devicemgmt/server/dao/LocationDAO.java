package com.devicemgmt.server.dao;

import com.devicemgmt.common.dto.LocationDTO;
import com.devicemgmt.server.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO {
    private static final Logger log = LoggerFactory.getLogger(LocationDAO.class);

    public List<LocationDTO> findAll() {
        List<LocationDTO> list = new ArrayList<>();
        String sql = """
            SELECT l.*, COUNT(d.id) AS device_count
            FROM locations l
            LEFT JOIN devices d ON d.location_id = l.id
            GROUP BY l.id ORDER BY l.name
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll locations error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int insert(LocationDTO l) {
        String sql = "INSERT INTO locations (name, description, floor) VALUES (?, ?, ?) RETURNING id";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, l.getName());
            ps.setString(2, l.getDescription());
            ps.setString(3, l.getFloor());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("insert location error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return -1;
    }

    public boolean update(LocationDTO l) {
        String sql = "UPDATE locations SET name=?, description=?, floor=?, updated_at=NOW() WHERE id=?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, l.getName());
            ps.setString(2, l.getDescription());
            ps.setString(3, l.getFloor());
            ps.setInt(4, l.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update location error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean delete(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM locations WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete location error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    private LocationDTO mapRow(ResultSet rs) throws SQLException {
        LocationDTO l = new LocationDTO();
        l.setId(rs.getInt("id"));
        l.setName(rs.getString("name"));
        l.setDescription(rs.getString("description"));
        l.setFloor(rs.getString("floor"));
        l.setDeviceCount(rs.getInt("device_count"));
        return l;
    }
}
