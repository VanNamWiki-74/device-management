package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.dao.ApprovalDAO;
import com.devicemgmt.server.dao.AssignmentDAO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

public class ApprovalService {
    private final ApprovalDAO dao = new ApprovalDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getAll(Request req) {
        int page = req.getPage() < 1 ? 1 : req.getPage();
        int pageSize = req.getPageSize() < 1 ? 20 : req.getPageSize();
        boolean isAdmin = TokenManager.getInstance().isAdmin(req.getToken());
        Integer requesterId = isAdmin ? null : TokenManager.getInstance().getUserId(req.getToken());

        var list = dao.findAll(requesterId, req.getFilter(), req.getKeyword(), page, pageSize);
        int total = dao.count(requesterId, req.getFilter(), req.getKeyword());
        return Response.ok("OK", gson.toJsonTree(list), total, page, pageSize);
    }

    public Response getTypes(Request req) {
        return Response.ok("OK", gson.toJsonTree(dao.findTypes()));
    }

    public Response create(Request req) {
        ApprovalDTO a = gson.fromJson(req.getData(), ApprovalDTO.class);
        if (a.getApprovalTypeId() <= 0) return Response.error("Vui lòng chọn loại yêu cầu.");
        if (a.getDescription() == null || a.getDescription().isBlank()) return Response.error("Vui lòng nhập mô tả/lý do.");

        int userId = TokenManager.getInstance().getUserId(req.getToken());
        a.setRequesterId(userId);

        // SỬA CHỮA (2) và THANH LÝ (3) phải chọn thiết bị đang được chính user này sử dụng
        if (a.getApprovalTypeId() == 2 || a.getApprovalTypeId() == 3) {
            if (a.getDeviceId() <= 0) return Response.error("Vui lòng chọn thiết bị cần sửa chữa/thanh lý.");
            if (!assignmentDAO.isActiveAssignment(a.getDeviceId(), userId)) {
                return Response.error("Bạn chỉ có thể gửi yêu cầu cho thiết bị đang được cấp cho mình.");
            }
        } else {
            a.setDeviceId(0);
        }

        int id = dao.insert(a);
        if (id > 0) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "CREATE_APPROVAL",
                "APPROVAL", id, null, "SUCCESS", a.getDescription());
            a.setId(id);
            return Response.ok("Gửi yêu cầu phê duyệt thành công.", gson.toJsonTree(a));
        }
        return Response.error("Gửi yêu cầu thất bại.");
    }

    public Response process(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Chỉ Admin mới có quyền phê duyệt.");
        int id = req.getData().get("id").getAsInt();
        String status = req.getData().get("status").getAsString();
        String comments = req.getData().has("comments") ? req.getData().get("comments").getAsString() : null;
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) return Response.error("Trạng thái không hợp lệ.");

        int approverId = TokenManager.getInstance().getUserId(req.getToken());
        boolean ok = dao.updateStatus(id, status, approverId, comments);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "PROCESS_APPROVAL",
                "APPROVAL", id, null, "SUCCESS", status);
            return Response.ok("Xử lý yêu cầu thành công.");
        }
        return Response.error("Xử lý yêu cầu thất bại.");
    }

    public Response markImported(Request req) {
        if (!TokenManager.getInstance().isAdmin(req.getToken())) return Response.error("Không có quyền.");
        int id = req.getData().get("id").getAsInt();
        boolean ok = dao.markImported(id);
        return ok ? Response.ok("Đã đánh dấu import.") : Response.error("Thất bại.");
    }
}
