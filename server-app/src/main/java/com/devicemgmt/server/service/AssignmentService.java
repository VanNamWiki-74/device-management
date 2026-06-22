package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.dao.AssignmentDAO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

import java.time.LocalDate;

public class AssignmentService {
    private final AssignmentDAO dao = new AssignmentDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getAll(Request req) {
        int page = req.getPage() < 1 ? 1 : req.getPage();
        int pageSize = req.getPageSize() < 1 ? 20 : req.getPageSize();
        var list = dao.findAll(req.getKeyword(), req.getFilter(), page, pageSize);
        int total = dao.count(req.getKeyword(), req.getFilter());
        return Response.ok("OK", gson.toJsonTree(list), total, page, pageSize);
    }

    public Response create(Request req) {
        AssignmentDTO a = gson.fromJson(req.getData(), AssignmentDTO.class);
        if (a.getDeviceId() <= 0) return Response.error("Vui lòng chọn thiết bị.");
        if (a.getAssignedTo() == null || a.getAssignedTo().isBlank()) return Response.error("Tên người nhận không được trống.");
        if (a.getAssignedDate() == null || a.getAssignedDate().isBlank()) {
            a.setAssignedDate(LocalDate.now().toString());
        }
        a.setAssignedBy(TokenManager.getInstance().getUserId(req.getToken()));
        int id = dao.insert(a);
        if (id > 0) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "CREATE_ASSIGNMENT",
                "ASSIGNMENT", id, a.getAssignedTo(), "SUCCESS", "Device ID: " + a.getDeviceId());
            a.setId(id);
            return Response.ok("Phân công thiết bị thành công.", gson.toJsonTree(a));
        }
        return Response.error("Phân công thất bại. Thiết bị có thể đang được sử dụng.");
    }

    public Response returnDevice(Request req) {
        int assignmentId = req.getData().get("id").getAsInt();
        int deviceId = req.getData().get("deviceId").getAsInt();
        String returnedDate = req.getData().has("returnedDate")
            ? req.getData().get("returnedDate").getAsString()
            : LocalDate.now().toString();

        boolean ok = dao.returnDevice(assignmentId, returnedDate, deviceId);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "RETURN_DEVICE",
                "ASSIGNMENT", assignmentId, null, "SUCCESS", "Device ID: " + deviceId);
            return Response.ok("Thu hồi thiết bị thành công.");
        }
        return Response.error("Thu hồi thiết bị thất bại.");
    }

    public Response delete(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        int id = req.getData().get("id").getAsInt();
        boolean ok = dao.delete(id);
        return ok ? Response.ok("Xóa thành công.") : Response.error("Xóa thất bại.");
    }
}
