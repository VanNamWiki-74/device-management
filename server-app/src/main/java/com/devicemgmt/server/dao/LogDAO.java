package com.devicemgmt.server.dao;

import com.devicemgmt.server.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogDAO {
    private static final Logger log = LoggerFactory.getLogger(LogDAO.class);

    public void insert(String username, String action, String targetType, Integer targetId,
                       String targetInfo, String result, String detail) {
        String sql = """
            INSERT INTO system_logs (username, action, target_type, target_id, target_info, result, detail)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, action);
            ps.setString(3, targetType);
            if (targetId != null) ps.setInt(4, targetId);
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, targetInfo);
            ps.setString(6, result);
            ps.setString(7, detail);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("LogDAO.insert error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public List<Map<String, Object>> findAll(int page, int pageSize, String keyword) {
        String sql = """
            SELECT * FROM system_logs
            WHERE (? IS NULL OR username ILIKE ? OR action ILIKE ? OR target_info ILIKE ?)
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            ps.setInt(5, pageSize);
            ps.setInt(6, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("username", rs.getString("username"));
                row.put("action", rs.getString("action"));
                row.put("targetType", rs.getString("target_type"));
                row.put("targetInfo", rs.getString("target_info"));
                row.put("result", rs.getString("result"));
                row.put("detail", rs.getString("detail"));
                Timestamp ts = rs.getTimestamp("created_at");
                row.put("createdAt", ts != null ? ts.toLocalDateTime().toString() : "");
                list.add(row);
            }
        } catch (SQLException e) {
            log.error("LogDAO.findAll error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int count(String keyword) {
        String sql = "SELECT COUNT(*) FROM system_logs WHERE (? IS NULL OR username ILIKE ? OR action ILIKE ? OR target_info ILIKE ?)";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("LogDAO.count error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public List<Map<String, Object>> getRecent(int limit) {
        String sql = "SELECT * FROM system_logs ORDER BY created_at DESC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("username"));
                row.put("action", rs.getString("action"));
                row.put("result", rs.getString("result"));
                Timestamp ts = rs.getTimestamp("created_at");
                row.put("createdAt", ts != null ? ts.toLocalDateTime().toString() : "");
                list.add(row);
            }
        } catch (SQLException e) {
            log.error("LogDAO.getRecent error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }
}
