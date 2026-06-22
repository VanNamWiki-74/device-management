package com.devicemgmt.server.dao;

import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.server.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApprovalDAO {
    private static final Logger log = LoggerFactory.getLogger(ApprovalDAO.class);

    private static final String SELECT_FULL = """
        SELECT a.*, d.code AS device_code, d.name AS device_name,
               t.name AS type_name, ru.full_name AS requester_name, au.full_name AS approver_name
        FROM approvals a
        LEFT JOIN devices d ON a.device_id = d.id
        LEFT JOIN approvals_types t ON a.approval_type = t.id
        LEFT JOIN users ru ON a.requester_id = ru.id
        LEFT JOIN users au ON a.approver_id = au.id
        """;

    public List<ApprovalDTO> findAll(Integer requesterId, String statusFilter, String keyword, int page, int pageSize) {
        String sql = SELECT_FULL + """
            WHERE (? IS NULL OR a.requester_id = ?)
            AND (? IS NULL OR a.status = ?)
            AND (? IS NULL OR d.code ILIKE ? OR d.name ILIKE ? OR ru.full_name ILIKE ? OR a.description ILIKE ?)
            ORDER BY a.id DESC
            LIMIT ? OFFSET ?
            """;
        List<ApprovalDTO> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            if (requesterId != null) { ps.setInt(1, requesterId); ps.setInt(2, requesterId); }
            else { ps.setNull(1, Types.INTEGER); ps.setNull(2, Types.INTEGER); }
            String sf = statusFilter != null && !statusFilter.isBlank() ? statusFilter : null;
            ps.setString(3, sf); ps.setString(4, sf);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            ps.setString(5, kw); ps.setString(6, kw); ps.setString(7, kw); ps.setString(8, kw); ps.setString(9, kw);
            ps.setInt(10, pageSize);
            ps.setInt(11, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll approvals error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int count(Integer requesterId, String statusFilter, String keyword) {
        String sql = """
            SELECT COUNT(*) FROM approvals a
            LEFT JOIN devices d ON a.device_id = d.id
            LEFT JOIN users ru ON a.requester_id = ru.id
            WHERE (? IS NULL OR a.requester_id = ?)
            AND (? IS NULL OR a.status = ?)
            AND (? IS NULL OR d.code ILIKE ? OR d.name ILIKE ? OR ru.full_name ILIKE ? OR a.description ILIKE ?)
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            if (requesterId != null) { ps.setInt(1, requesterId); ps.setInt(2, requesterId); }
            else { ps.setNull(1, Types.INTEGER); ps.setNull(2, Types.INTEGER); }
            String sf = statusFilter != null && !statusFilter.isBlank() ? statusFilter : null;
            ps.setString(3, sf); ps.setString(4, sf);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            ps.setString(5, kw); ps.setString(6, kw); ps.setString(7, kw); ps.setString(8, kw); ps.setString(9, kw);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("count approvals error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public ApprovalDTO findById(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + " WHERE a.id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            log.error("findById approval error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return null;
    }

    public int insert(ApprovalDTO a) {
        String sql = """
            INSERT INTO approvals (requester_id, device_id, approval_type, description, status)
            VALUES (?, ?, ?, ?, 'PENDING')
            RETURNING id
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, a.getRequesterId());
            if (a.getDeviceId() > 0) ps.setInt(2, a.getDeviceId());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, a.getApprovalTypeId());
            ps.setString(4, a.getDescription());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("insert approval error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return -1;
    }

    public boolean updateStatus(int id, String status, int approverId, String comments) {
        String sql = """
            UPDATE approvals SET status=?, approver_id=?, comments=?, approval_date=NOW()
            WHERE id=?
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, approverId);
            ps.setString(3, comments);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateStatus approval error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean markImported(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE approvals SET imported=TRUE WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("markImported approval error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public List<java.util.Map<String, Object>> findTypes() {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            ResultSet rs = conn.prepareStatement("SELECT id, name, description FROM approvals_types ORDER BY id").executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("name", rs.getString("name"));
                m.put("description", rs.getString("description"));
                list.add(m);
            }
        } catch (SQLException e) {
            log.error("findTypes error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public String findTypeName(int typeId) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT name FROM approvals_types WHERE id = ?");
            ps.setInt(1, typeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {
            log.error("findTypeName error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return null;
    }

    private ApprovalDTO mapRow(ResultSet rs) throws SQLException {
        ApprovalDTO a = new ApprovalDTO();
        a.setId(rs.getInt("id"));
        a.setRequesterId(rs.getInt("requester_id"));
        a.setRequesterName(rs.getString("requester_name"));
        a.setDeviceId(rs.getInt("device_id"));
        a.setDeviceCode(rs.getString("device_code"));
        a.setDeviceName(rs.getString("device_name"));
        a.setApprovalTypeId(rs.getInt("approval_type"));
        a.setApprovalTypeName(rs.getString("type_name"));
        a.setDescription(rs.getString("description"));
        a.setStatus(rs.getString("status"));
        a.setApproverId(rs.getInt("approver_id"));
        a.setApproverName(rs.getString("approver_name"));
        a.setComments(rs.getString("comments"));
        a.setImported(rs.getBoolean("imported"));
        Timestamp rd = rs.getTimestamp("request_date");
        if (rd != null) a.setRequestDate(rd.toLocalDateTime().toString());
        Timestamp ad = rs.getTimestamp("approval_date");
        if (ad != null) a.setApprovalDate(ad.toLocalDateTime().toString());
        return a;
    }
}
