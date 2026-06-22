package com.devicemgmt.server.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.server.db.ConnectionManager;

public class AssignmentDAO {
    private static final Logger log = LoggerFactory.getLogger(AssignmentDAO.class);

    private static final String SELECT_FULL = """
        SELECT a.*, d.code AS device_code, d.name AS device_name, u.full_name AS assigned_by_name
        FROM assignments a
        LEFT JOIN devices d ON a.device_id = d.id
        LEFT JOIN users u ON a.assigned_by = u.id
        """;

    public List<AssignmentDTO> findAll(String keyword, String statusFilter, int page, int pageSize) {
        String sql = SELECT_FULL + """
            WHERE (? IS NULL OR a.assigned_to ILIKE ? OR d.code ILIKE ? OR d.name ILIKE ? OR a.department ILIKE ?)
            AND (? IS NULL OR a.status = ?)
            ORDER BY a.id DESC
            LIMIT ? OFFSET ?
            """;
        List<AssignmentDTO> list = new ArrayList<>();
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
            log.error("findAll assignments error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int count(String keyword, String statusFilter) {
        String sql = """
            SELECT COUNT(*) FROM assignments a
            LEFT JOIN devices d ON a.device_id = d.id
            WHERE (? IS NULL OR a.assigned_to ILIKE ? OR d.code ILIKE ? OR d.name ILIKE ? OR a.department ILIKE ?)
            AND (? IS NULL OR a.status = ?)
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
            log.error("count assignments error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public int insert(AssignmentDTO a) {
        String sql = """
            INSERT INTO assignments (
                device_id,
                user_id,
                assigned_to,
                department,
                assigned_by,
                assigned_date,
                expected_return,
                status,
                notes
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
            RETURNING id
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, a.getDeviceId());
            ps.setInt(2, a.getUserId());
            ps.setString(3, a.getAssignedTo());
            ps.setString(4, a.getDepartment());
            if (a.getAssignedBy() > 0) ps.setInt(5, a.getAssignedBy());
            else ps.setNull(5, Types.INTEGER);
            ps.setDate(6, Date.valueOf(a.getAssignedDate()));
            if (a.getExpectedReturn() != null && !a.getExpectedReturn().isBlank())
                ps.setDate(7, Date.valueOf(a.getExpectedReturn()));
            else ps.setNull(7, Types.DATE);
            ps.setString(8, a.getNotes());
            ResultSet rs = ps.executeQuery();
            int id = -1;
            if (rs.next()) id = rs.getInt(1);

            // Update device status to IN_USE
            if (id > 0) {
                PreparedStatement update = conn.prepareStatement(
                    "UPDATE devices SET status='IN_USE', updated_at=NOW() WHERE id=?");
                update.setInt(1, a.getDeviceId());
                update.executeUpdate();
            }
            conn.commit();
            return id;
        } catch (SQLException e) {
            log.error("insert assignment error: {}", e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean returnDevice(int assignmentId, String returnedDate, int deviceId) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE assignments SET status='RETURNED', returned_date=?, updated_at=NOW() WHERE id=?");
            ps.setDate(1, Date.valueOf(returnedDate));
            ps.setInt(2, assignmentId);
            ps.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(
                "UPDATE devices SET status='AVAILABLE', updated_at=NOW() WHERE id=?");
            ps2.setInt(1, deviceId);
            ps2.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            log.error("returnDevice error: {}", e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean isActiveAssignment(int deviceId, int userId) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM assignments WHERE device_id=? AND user_id=? AND status='ACTIVE'");
            ps.setInt(1, deviceId);
            ps.setInt(2, userId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            log.error("isActiveAssignment error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean delete(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM assignments WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete assignment error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    private AssignmentDTO mapRow(ResultSet rs) throws SQLException {
        AssignmentDTO a = new AssignmentDTO();
        a.setId(rs.getInt("id"));
        a.setDeviceId(rs.getInt("device_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setDeviceCode(rs.getString("device_code"));
        a.setDeviceName(rs.getString("device_name"));
        a.setAssignedTo(rs.getString("assigned_to"));
        a.setDepartment(rs.getString("department"));
        a.setAssignedBy(rs.getInt("assigned_by"));
        a.setAssignedByName(rs.getString("assigned_by_name"));
        Date ad = rs.getDate("assigned_date");
        if (ad != null) a.setAssignedDate(ad.toString());
        Date er = rs.getDate("expected_return");
        if (er != null) a.setExpectedReturn(er.toString());
        Date rd = rs.getDate("returned_date");
        if (rd != null) a.setReturnedDate(rd.toString());
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime().toString());
        return a;
    }
}
