package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.CategoryDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.dao.CategoryDAO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

public class CategoryService {
    private final CategoryDAO dao = new CategoryDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getAll(Request req) {
        return Response.ok("OK", gson.toJsonTree(dao.findAll()));
    }

    public Response create(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        CategoryDTO c = gson.fromJson(req.getData(), CategoryDTO.class);
        if (c.getName() == null || c.getName().isBlank()) return Response.error("Tên danh mục không được trống.");
        if (dao.nameExists(c.getName(), 0)) return Response.error("Danh mục đã tồn tại.");
        int id = dao.insert(c);
        if (id > 0) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "CREATE_CATEGORY", "CATEGORY", id, c.getName(), "SUCCESS", null);
            c.setId(id);
            return Response.ok("Thêm danh mục thành công.", gson.toJsonTree(c));
        }
        return Response.error("Thêm danh mục thất bại.");
    }

    public Response update(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        CategoryDTO c = gson.fromJson(req.getData(), CategoryDTO.class);
        if (c.getName() == null || c.getName().isBlank()) return Response.error("Tên danh mục không được trống.");
        if (dao.nameExists(c.getName(), c.getId())) return Response.error("Tên danh mục đã tồn tại.");
        boolean ok = dao.update(c);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "UPDATE_CATEGORY", "CATEGORY", c.getId(), c.getName(), "SUCCESS", null);
            return Response.ok("Cập nhật thành công.");
        }
        return Response.error("Cập nhật thất bại.");
    }

    public Response delete(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        int id = req.getData().get("id").getAsInt();
        boolean ok = dao.delete(id);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "DELETE_CATEGORY", "CATEGORY", id, null, "SUCCESS", null);
            return Response.ok("Xóa danh mục thành công.");
        }
        return Response.error("Xóa thất bại. Danh mục có thể đang được sử dụng.");
    }
}
