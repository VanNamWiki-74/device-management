package com.devicemgmt.server.handler;

import com.devicemgmt.common.constants.Actions;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.security.TokenManager;
import com.devicemgmt.server.service.*;

public class RequestRouter {
    private final AuthService authService = new AuthService();
    private final DeviceService deviceService = new DeviceService();
    private final CategoryService categoryService = new CategoryService();
    private final LocationService locationService = new LocationService();
    private final AssignmentService assignmentService = new AssignmentService();
    private final UserService userService = new UserService();
    private final DashboardService dashboardService = new DashboardService();
    private final ApprovalService approvalService = new ApprovalService();

    public Response route(Request req) {
        if (req == null || req.getAction() == null) return Response.error("Invalid request");

        // Public actions (no auth required)
        if (Actions.LOGIN.equals(req.getAction())) return authService.login(req);

        // All other actions require valid token
        if (!TokenManager.getInstance().isValid(req.getToken())) {
            return Response.error("Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.");
        }

        return switch (req.getAction()) {
            // Auth
            case Actions.LOGOUT          -> authService.logout(req);
            case Actions.CHANGE_PASSWORD -> authService.changePassword(req);

            // Devices
            case Actions.GET_DEVICES     -> deviceService.getAll(req);
            case Actions.CREATE_DEVICE   -> deviceService.create(req);
            case Actions.UPDATE_DEVICE   -> deviceService.update(req);
            case Actions.DELETE_DEVICE   -> deviceService.delete(req);
            case Actions.EXPORT_DEVICES  -> deviceService.exportAll(req);
            case Actions.IMPORT_DEVICES  -> deviceService.importDevices(req);

            // Categories
            case Actions.GET_CATEGORIES  -> categoryService.getAll(req);
            case Actions.CREATE_CATEGORY -> categoryService.create(req);
            case Actions.UPDATE_CATEGORY -> categoryService.update(req);
            case Actions.DELETE_CATEGORY -> categoryService.delete(req);

            // Locations
            case Actions.GET_LOCATIONS   -> locationService.getAll(req);
            case Actions.CREATE_LOCATION -> locationService.create(req);
            case Actions.UPDATE_LOCATION -> locationService.update(req);
            case Actions.DELETE_LOCATION -> locationService.delete(req);

            // Assignments
            case Actions.GET_ASSIGNMENTS   -> assignmentService.getAll(req);
            case Actions.CREATE_ASSIGNMENT -> assignmentService.create(req);
            case Actions.RETURN_DEVICE     -> assignmentService.returnDevice(req);
            case Actions.DELETE_ASSIGNMENT -> assignmentService.delete(req);

            // Approvals
            case Actions.GET_APPROVALS         -> approvalService.getAll(req);
            case Actions.GET_APPROVAL_TYPES     -> approvalService.getTypes(req);
            case Actions.CREATE_APPROVAL        -> approvalService.create(req);
            case Actions.PROCESS_APPROVAL       -> approvalService.process(req);
            case Actions.MARK_APPROVAL_IMPORTED -> approvalService.markImported(req);

            // Users
            case Actions.GET_USERS   -> userService.getAll(req);
            case Actions.CREATE_USER -> userService.create(req);
            case Actions.UPDATE_USER -> userService.update(req);
            case Actions.DELETE_USER -> userService.delete(req);

            // Dashboard & Logs
            case Actions.GET_DASHBOARD -> dashboardService.getDashboard(req);
            case Actions.GET_LOGS      -> dashboardService.getLogs(req);

            default -> Response.error("Hành động không được hỗ trợ: " + req.getAction());
        };
    }
}
