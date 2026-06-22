package com.devicemgmt.server.dao;

import com.devicemgmt.common.dto.UserDTO;
import com.devicemgmt.server.db.ConnectionManager;
import com.devicemgmt.server.security.AESUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);

    private static final String SELECT_FULL = """
        SELECT u.*, r.name AS role
        FROM users u
        LEFT JOIN user_roles r ON u.role_id = r.id
        """;

    public UserDTO findByUsername(String username) {
        String sql = SELECT_FULL + " WHERE u.username = ?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            log.error("findByUsername error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return null;
    }

    public String getPasswordHash(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("password_hash");
        } catch (SQLException e) {
            log.error("getPasswordHash error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return null;
    }

    public List<UserDTO> findAll(String keyword, int page, int pageSize) {
        String sql = SELECT_FULL + """
            WHERE (? IS NULL OR u.username ILIKE ? OR u.full_name ILIKE ?)
            ORDER BY u.id
            LIMIT ? OFFSET ?
            """;
        List<UserDTO> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setInt(4, pageSize);
            ps.setInt(5, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll users error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int count(String keyword) {
        String sql = "SELECT COUNT(*) FROM users WHERE (? IS NULL OR username ILIKE ? OR full_name ILIKE ?)";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = keyword != null && !keyword.isBlank() ? "%" + keyword + "%" : null;
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("count users error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public boolean insert(UserDTO u, String passwordHash) {
        String sql = """
            INSERT INTO users (username, password_hash, full_name, email, phone, role_id, is_active)
            VALUES (?, ?, ?, ?, ?, (SELECT id FROM user_roles WHERE name = ?), ?)
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getUsername());
            ps.setString(2, passwordHash);
            ps.setString(3, u.getFullName());
            ps.setString(4, AESUtils.encrypt(u.getEmail()));
            ps.setString(5, AESUtils.encrypt(u.getPhone()));
            ps.setString(6, u.getRole());
            ps.setBoolean(7, u.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("insert user error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean update(UserDTO u) {
        String sql = """
            UPDATE users SET full_name=?, email=?, phone=?, role_id=(SELECT id FROM user_roles WHERE name=?), is_active=?, updated_at=NOW()
            WHERE id=?
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getFullName());
            ps.setString(2, AESUtils.encrypt(u.getEmail()));
            ps.setString(3, AESUtils.encrypt(u.getPhone()));
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isActive());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update user error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean updatePassword(int userId, String newHash) {
        String sql = "UPDATE users SET password_hash=?, updated_at=NOW() WHERE id=?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updatePassword error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean delete(int id) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM users WHERE id=? AND role_id != (SELECT id FROM user_roles WHERE name='ADMIN')");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete user error: {}", e.getMessage());
            return false;
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public void incrementFailedLogin(String username) {
        String sql = """
            UPDATE users SET failed_login_count = failed_login_count + 1,
            locked_until = CASE WHEN failed_login_count + 1 >= 5
                THEN NOW() + INTERVAL '15 minutes' ELSE locked_until END
            WHERE username = ?
            """;
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("incrementFailedLogin error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public void resetFailedLogin(String username) {
        String sql = "UPDATE users SET failed_login_count=0, locked_until=NULL WHERE username=?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("resetFailedLogin error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
    }

    public boolean isLocked(String username) {
        String sql = "SELECT locked_until FROM users WHERE username=?";
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp locked = rs.getTimestamp("locked_until");
                return locked != null && locked.after(new java.util.Date());
            }
        } catch (SQLException e) {
            log.error("isLocked error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return false;
    }

    private UserDTO mapRow(ResultSet rs) throws SQLException {
        UserDTO u = new UserDTO();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(AESUtils.decrypt(rs.getString("email")));
        u.setPhone(AESUtils.decrypt(rs.getString("phone")));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime().toString());
        return u;
    }
}
