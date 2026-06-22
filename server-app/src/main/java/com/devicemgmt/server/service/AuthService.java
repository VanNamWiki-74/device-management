package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.common.dto.UserDTO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.dao.UserDAO;
import com.devicemgmt.server.security.PasswordUtils;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response login(Request req) {
        String username = req.getData().get("username").getAsString();
        String password = req.getData().get("password").getAsString();

        if (userDAO.isLocked(username)) {
            logDAO.insert(username, "LOGIN", null, null, null, "FAILED", "Account locked");
            return Response.error("Tài khoản bị khóa tạm thời do đăng nhập sai nhiều lần. Vui lòng thử lại sau.");
        }

        UserDTO user = userDAO.findByUsername(username);
        if (user == null) {
            return Response.error("Tên đăng nhập không tồn tại.");
        }

        if (!user.isActive()) {
            return Response.error("Tài khoản đã bị vô hiệu hóa.");
        }

        String hash = userDAO.getPasswordHash(username);
        if (!PasswordUtils.verify(password, hash)) {
            userDAO.incrementFailedLogin(username);
            logDAO.insert(username, "LOGIN", null, null, null, "FAILED", "Wrong password");
            return Response.error("Mật khẩu không đúng.");
        }


        userDAO.resetFailedLogin(username);
        String token = TokenManager.getInstance().createToken(user.getId(), username, user.getRole());
        user.setToken(token);

        logDAO.insert(username, "LOGIN", null, null, null, "SUCCESS", null);
        return Response.ok("Đăng nhập thành công", gson.toJsonTree(user));
    }

    public Response logout(Request req) {
        String token = req.getToken();
        if (token != null) {
            String username = TokenManager.getInstance().getUsername(token);
            TokenManager.getInstance().invalidate(token);
            logDAO.insert(username, "LOGOUT", null, null, null, "SUCCESS", null);
        }
        return Response.ok("Đăng xuất thành công");
    }

    public Response changePassword(Request req) {
        String token = req.getToken();
        if (!TokenManager.getInstance().isValid(token)) {
            return Response.error("Phiên đăng nhập hết hạn.");
        }

        String oldPassword = req.getData().get("oldPassword").getAsString();
        String newPassword = req.getData().get("newPassword").getAsString();

        if (!PasswordUtils.isStrongPassword(newPassword)) {
            return Response.error("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        int userId = TokenManager.getInstance().getUserId(token);
        String username = TokenManager.getInstance().getUsername(token);
        String currentHash = userDAO.getPasswordHash(username);

        if (!PasswordUtils.verify(oldPassword, currentHash)) {
            return Response.error("Mật khẩu hiện tại không đúng.");
        }

        boolean ok = userDAO.updatePassword(userId, PasswordUtils.hash(newPassword));
        if (ok) {
            logDAO.insert(username, "CHANGE_PASSWORD", "USER", userId, username, "SUCCESS", null);
            return Response.ok("Đổi mật khẩu thành công.");
        }
        return Response.error("Đổi mật khẩu thất bại.");
    }
}
