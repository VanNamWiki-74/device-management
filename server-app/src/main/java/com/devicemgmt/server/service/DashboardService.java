package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.DashboardDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.db.ConnectionManager;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getDashboard(Request req) {
        DashboardDTO dto = new DashboardDTO();
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();

            // Device counts by status
            ResultSet rs = conn.prepareStatement("""
                SELECT status, COUNT(*) AS cnt FROM devices GROUP BY status
                """).executeQuery();
            int total = 0;
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                total += cnt;
                switch (rs.getString("status")) {
                    case "AVAILABLE"   -> dto.setAvailableDevices(cnt);
                    case "IN_USE"      -> dto.setInUseDevices(cnt);
                    case "MAINTENANCE" -> dto.setMaintenanceDevices(cnt);
                    case "BROKEN"      -> dto.setBrokenDevices(cnt);
                    case "DISPOSED"    -> dto.setDisposedDevices(cnt);
                }
            }
            dto.setTotalDevices(total);

            rs = conn.prepareStatement("SELECT COUNT(*) FROM device_categories").executeQuery();
            if (rs.next()) dto.setTotalCategories(rs.getInt(1));

            rs = conn.prepareStatement("SELECT COUNT(*) FROM locations").executeQuery();
            if (rs.next()) dto.setTotalLocations(rs.getInt(1));

            rs = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE is_active=true").executeQuery();
            if (rs.next()) dto.setTotalUsers(rs.getInt(1));

            rs = conn.prepareStatement("SELECT COUNT(*) FROM assignments WHERE status='ACTIVE'").executeQuery();
            if (rs.next()) dto.setActiveAssignments(rs.getInt(1));

            // Devices by category
            rs = conn.prepareStatement("""
                SELECT dc.name, COUNT(d.id) AS cnt
                FROM device_categories dc
                LEFT JOIN devices d ON d.category_id = dc.id
                GROUP BY dc.name ORDER BY cnt DESC LIMIT 10
                """).executeQuery();
            List<Map<String, Object>> byCategory = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", rs.getString("name"));
                item.put("count", rs.getInt("cnt"));
                byCategory.add(item);
            }
            dto.setDevicesByCategory(byCategory);

            dto.setRecentLogs(logDAO.getRecent(10));

        } catch (Exception e) {
            log.error("getDashboard error: {}", e.getMessage());
        } finally {
            ConnectionManager.getInstance().releaseConnection(conn);
        }
        return Response.ok("OK", gson.toJsonTree(dto));
    }

    public Response getLogs(Request req) {
        int page = req.getPage() < 1 ? 1 : req.getPage();
        int pageSize = req.getPageSize() < 1 ? 20 : req.getPageSize();
        var list = logDAO.findAll(page, pageSize, req.getKeyword());
        int total = logDAO.count(req.getKeyword());
        return Response.ok("OK", gson.toJsonTree(list), total, page, pageSize);
    }
}
