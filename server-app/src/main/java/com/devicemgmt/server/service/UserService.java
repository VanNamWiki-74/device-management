package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.common.dto.UserDTO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.dao.UserDAO;
import com.devicemgmt.server.security.PasswordUtils;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

public class UserService {
    private final UserDAO dao = new UserDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getAll(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        int page = req.getPage() < 1 ? 1 : req.getPage();
        int pageSize = req.getPageSize() < 1 ? 20 : req.getPageSize();
        var list = dao.findAll(req.getKeyword(), page, pageSize);
        int total = dao.count(req.getKeyword());
        return Response.ok("OK", gson.toJsonTree(list), total, page, pageSize);
    }

    public Response create(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        UserDTO u = gson.fromJson(req.getData(), UserDTO.class);
        String password = req.getData().has("password") ? req.getData().get("password").getAsString() : "User@123";

        if (u.getUsername() == null || u.getUsername().isBlank()) return Response.error("Tên đăng nhập không được trống.");
        if (dao.findByUsername(u.getUsername()) != null) return Response.error("Tên đăng nhập đã tồn tại.");
        if (!PasswordUtils.isStrongPassword(password)) return Response.error("Mật khẩu phải có ít nhất 6 ký tự.");

        u.setActive(true);
        if (u.getRole() == null) u.setRole("USER");

        boolean ok = dao.insert(u, PasswordUtils.hash(password));
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "CREATE_USER", "USER", 0, u.getUsername(), "SUCCESS", null);
            return Response.ok("Tạo tài khoản thành công.");
        }
        return Response.error("Tạo tài khoản thất bại.");
    }

    public Response update(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        UserDTO u = gson.fromJson(req.getData(), UserDTO.class);
        boolean ok = dao.update(u);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "UPDATE_USER", "USER", u.getId(), u.getUsername(), "SUCCESS", null);
            return Response.ok("Cập nhật tài khoản thành công.");
        }
        return Response.error("Cập nhật thất bại.");
    }

    public Response delete(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        int id = req.getData().get("id").getAsInt();
        // Prevent deleting self
        if (id == TokenManager.getInstance().getUserId(req.getToken())) {
            return Response.error("Không thể xóa tài khoản đang đăng nhập.");
        }
        boolean ok = dao.delete(id);
        return ok ? Response.ok("Xóa tài khoản thành công.") : Response.error("Xóa thất bại. Không thể xóa tài khoản Admin.");
    }
}
