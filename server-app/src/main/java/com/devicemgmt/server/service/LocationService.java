package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.LocationDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.dao.LocationDAO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

public class LocationService {
    private final LocationDAO dao = new LocationDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getAll(Request req) {
        return Response.ok("OK", gson.toJsonTree(dao.findAll()));
    }

    public Response create(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        LocationDTO l = gson.fromJson(req.getData(), LocationDTO.class);
        if (l.getName() == null || l.getName().isBlank()) return Response.error("Tên vị trí không được trống.");
        int id = dao.insert(l);
        if (id > 0) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "CREATE_LOCATION", "LOCATION", id, l.getName(), "SUCCESS", null);
            l.setId(id);
            return Response.ok("Thêm vị trí thành công.", gson.toJsonTree(l));
        }
        return Response.error("Thêm vị trí thất bại. Tên có thể đã tồn tại.");
    }

    public Response update(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        LocationDTO l = gson.fromJson(req.getData(), LocationDTO.class);
        if (l.getName() == null || l.getName().isBlank()) return Response.error("Tên vị trí không được trống.");
        boolean ok = dao.update(l);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "UPDATE_LOCATION", "LOCATION", l.getId(), l.getName(), "SUCCESS", null);
            return Response.ok("Cập nhật thành công.");
        }
        return Response.error("Cập nhật thất bại.");
    }

    public Response delete(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        int id = req.getData().get("id").getAsInt();
        boolean ok = dao.delete(id);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "DELETE_LOCATION", "LOCATION", id, null, "SUCCESS", null);
            return Response.ok("Xóa vị trí thành công.");
        }
        return Response.error("Xóa thất bại. Vị trí có thể đang có thiết bị.");
    }
}
