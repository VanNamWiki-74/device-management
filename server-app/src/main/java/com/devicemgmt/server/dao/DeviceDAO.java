package com.devicemgmt.server.dao;

import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.server.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceDAO {
    private static final Logger log = LoggerFactory.getLogger(DeviceDAO.class);

    private static final String SELECT_FULL = """
        SELECT d.*, dc.name AS category_name, l.name AS location_name
        FROM devices d
        LEFT JOIN device_categories dc ON d.category_id = dc.id
        LEFT JOIN locations l ON d.location_id = l.id
        """;

    public List<DeviceDTO> findAll(String keyword, String statusFilter, int page, int pageSize) {
        String sql = SELECT_FULL + """
            WHERE (? IS NULL OR d.code ILIKE ? OR d.name ILIKE ? OR d.brand ILIKE ? OR d.serial_number ILIKE ?)
            AND (? IS NULL OR d.status = ?)
            ORDER BY d.id DESC
            LIMIT ? OFFSET ?
            """;
        List<DeviceDTO> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            String sf = statusFilter != null && !statusFilter.isBlank() ? statusFilter : null;
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ps.setString(4, kw); ps.setString(5, kw);
            ps.setString(6, sf); ps.setString(7, sf);
            ps.setInt(8, pageSize);
            ps.setInt(9, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll devices error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<DeviceDTO> findAllForUser(int userId, String keyword, String statusFilter, int page, int pageSize) {
        String sql = SELECT_FULL + """
            INNER JOIN assignments asg ON asg.device_id = d.id AND asg.user_id = ? AND asg.status = 'ACTIVE'
            WHERE (? IS NULL OR d.code ILIKE ? OR d.name ILIKE ? OR d.brand ILIKE ? OR d.serial_number ILIKE ?)
            AND (? IS NULL OR d.status = ?)
            ORDER BY d.id DESC
            LIMIT ? OFFSET ?
            """;
        List<DeviceDTO> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            String sf = statusFilter != null && !statusFilter.isBlank() ? statusFilter : null;
            ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            ps.setString(5, kw); ps.setString(6, kw);
            ps.setString(7, sf); ps.setString(8, sf);
            ps.setInt(9, pageSize);
            ps.setInt(10, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAllForUser devices error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int countForUser(int userId, String keyword, String statusFilter) {
        String sql = """
            SELECT COUNT(*) FROM devices d
            INNER JOIN assignments asg ON asg.device_id = d.id AND asg.user_id = ? AND asg.status = 'ACTIVE'
            WHERE (? IS NULL OR d.code ILIKE ? OR d.name ILIKE ? OR d.brand ILIKE ? OR d.serial_number ILIKE ?)
            AND (? IS NULL OR d.status = ?)
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            String sf = statusFilter != null && !statusFilter.isBlank() ? statusFilter : null;
            ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            ps.setString(5, kw); ps.setString(6, kw);
            ps.setString(7, sf); ps.setString(8, sf);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("countForUser devices error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public int count(String keyword, String statusFilter) {
        String sql = """
            SELECT COUNT(*) FROM devices d
            WHERE (? IS NULL OR d.code ILIKE ? OR d.name ILIKE ? OR d.brand ILIKE ? OR d.serial_number ILIKE ?)
            AND (? IS NULL OR d.status = ?)
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            String sf = statusFilter != null && !statusFilter.isBlank() ? statusFilter : null;
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ps.setString(4, kw); ps.setString(5, kw);
            ps.setString(6, sf); ps.setString(7, sf);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("count devices error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public DeviceDTO findById(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + " WHERE d.id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            log.error("findById device error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return null;
    }

    public boolean codeExists(String code, int excludeId) {
        String sql = "SELECT 1 FROM devices WHERE code = ? AND id != ?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            log.error("codeExists error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return false;
    }

    public int insert(DeviceDTO d) {
        String sql = """
            INSERT INTO devices (code, name, category_id, location_id, brand, model,
                serial_number, status, purchase_date, warranty_expiry, purchase_price, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, d.getCode());
            ps.setString(2, d.getName());
            setNullableInt(ps, 3, d.getCategoryId());
            setNullableInt(ps, 4, d.getLocationId());
            ps.setString(5, d.getBrand());
            ps.setString(6, d.getModel());
            ps.setString(7, d.getSerialNumber());
            ps.setString(8, d.getStatus() != null ? d.getStatus() : "AVAILABLE");
            setNullableDate(ps, 9, d.getPurchaseDate());
            setNullableDate(ps, 10, d.getWarrantyExpiry());
            if (d.getPurchasePrice() > 0) ps.setDouble(11, d.getPurchasePrice());
            else ps.setNull(11, Types.NUMERIC);
            ps.setString(12, d.getNotes());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("insert device error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return -1;
    }

    public boolean update(DeviceDTO d) {
        String sql = """
            UPDATE devices SET code=?, name=?, category_id=?, location_id=?, brand=?, model=?,
                serial_number=?, status=?, purchase_date=?, warranty_expiry=?, purchase_price=?, notes=?, updated_at=NOW()
            WHERE id=?
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, d.getCode());
            ps.setString(2, d.getName());
            setNullableInt(ps, 3, d.getCategoryId());
            setNullableInt(ps, 4, d.getLocationId());
            ps.setString(5, d.getBrand());
            ps.setString(6, d.getModel());
            ps.setString(7, d.getSerialNumber());
            ps.setString(8, d.getStatus());
            setNullableDate(ps, 9, d.getPurchaseDate());
            setNullableDate(ps, 10, d.getWarrantyExpiry());
            if (d.getPurchasePrice() > 0) ps.setDouble(11, d.getPurchasePrice());
            else ps.setNull(11, Types.NUMERIC);
            ps.setString(12, d.getNotes());
            ps.setInt(13, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update device error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean delete(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM devices WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete device error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public List<DeviceDTO> findAll() {
        List<DeviceDTO> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            ResultSet rs = conn.prepareStatement(SELECT_FULL + " ORDER BY d.code").executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    private void setNullableInt(PreparedStatement ps, int idx, int val) throws SQLException {
        if (val > 0) ps.setInt(idx, val);
        else ps.setNull(idx, Types.INTEGER);
    }

    private void setNullableDate(PreparedStatement ps, int idx, String dateStr) throws SQLException {
        if (dateStr != null && !dateStr.isBlank()) {
            try { ps.setDate(idx, Date.valueOf(dateStr)); }
            catch (Exception e) { ps.setNull(idx, Types.DATE); }
        } else {
            ps.setNull(idx, Types.DATE);
        }
    }

    private DeviceDTO mapRow(ResultSet rs) throws SQLException {
        DeviceDTO d = new DeviceDTO();
        d.setId(rs.getInt("id"));
        d.setCode(rs.getString("code"));
        d.setName(rs.getString("name"));
        d.setCategoryId(rs.getInt("category_id"));
        d.setCategoryName(rs.getString("category_name"));
        d.setLocationId(rs.getInt("location_id"));
        d.setLocationName(rs.getString("location_name"));
        d.setBrand(rs.getString("brand"));
        d.setModel(rs.getString("model"));
        d.setSerialNumber(rs.getString("serial_number"));
        d.setStatus(rs.getString("status"));
        Date pd = rs.getDate("purchase_date");
        if (pd != null) d.setPurchaseDate(pd.toString());
        Date wd = rs.getDate("warranty_expiry");
        if (wd != null) d.setWarrantyExpiry(wd.toString());
        d.setPurchasePrice(rs.getDouble("purchase_price"));
        d.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) d.setCreatedAt(ca.toLocalDateTime().toString());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) d.setUpdatedAt(ua.toLocalDateTime().toString());
        return d;
    }
}
