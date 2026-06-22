package com.devicemgmt.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.devicemgmt.client.ServerConnection;
import com.devicemgmt.common.constants.Actions;
import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.common.dto.CategoryDTO;
import com.devicemgmt.common.dto.DashboardDTO;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.LocationDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.common.dto.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class ClientService {
    private static final ClientService INSTANCE = new ClientService();
    private final ServerConnection conn = ServerConnection.getInstance();
    private final Gson gson = new Gson();

    private String token;
    private UserDTO currentUser;

    private ClientService() {}

    public static ClientService getInstance() { return INSTANCE; }

    
    public Response login(String username, String password) {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        Request req = new Request(Actions.LOGIN, data);
        Response resp = conn.send(req);
        if (resp.isSuccess() && resp.getData() != null) {
            currentUser = gson.fromJson(resp.getData(), UserDTO.class);
            token = currentUser.getToken();
        }
        return resp;
    }

    public Response logout() {
        Request req = new Request(Actions.LOGOUT);
        req.setToken(token);
        Response resp = conn.send(req);
        token = null;
        currentUser = null;
        return resp;
    }

    public Response changePassword(String oldPwd, String newPwd) {
        JsonObject data = new JsonObject();
        data.addProperty("oldPassword", oldPwd);
        data.addProperty("newPassword", newPwd);
        return sendAuth(Actions.CHANGE_PASSWORD, data);
    }

    
    public Response getDevices(String keyword, String filter, int page, int pageSize) {
        Request req = authRequest(Actions.GET_DEVICES);
        req.setKeyword(keyword); req.setFilter(filter);
        req.setPage(page); req.setPageSize(pageSize);
        return conn.send(req);
    }

    public List<DeviceDTO> getDeviceList(String keyword, String filter, int page, int pageSize) {
        Response resp = getDevices(keyword, filter, page, pageSize);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<DeviceDTO>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public Response createDevice(DeviceDTO d) {
        return sendAuth(Actions.CREATE_DEVICE, gson.toJsonTree(d).getAsJsonObject());
    }

    public Response updateDevice(DeviceDTO d) {
        return sendAuth(Actions.UPDATE_DEVICE, gson.toJsonTree(d).getAsJsonObject());
    }

    public Response deleteDevice(int id) {
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        return sendAuth(Actions.DELETE_DEVICE, data);
    }

    public Response exportDevices() {
        return sendAuth(Actions.EXPORT_DEVICES, new JsonObject());
    }

    public Response importDevices(String csv) {
        JsonObject data = new JsonObject();
        data.addProperty("csv", csv);
        return sendAuth(Actions.IMPORT_DEVICES, data);
    }

   
    public List<CategoryDTO> getCategories() {
        Response resp = sendAuth(Actions.GET_CATEGORIES, null);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<CategoryDTO>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public Response createCategory(CategoryDTO c) {
        return sendAuth(Actions.CREATE_CATEGORY, gson.toJsonTree(c).getAsJsonObject());
    }

    public Response updateCategory(CategoryDTO c) {
        return sendAuth(Actions.UPDATE_CATEGORY, gson.toJsonTree(c).getAsJsonObject());
    }

    public Response deleteCategory(int id) {
        JsonObject data = new JsonObject(); data.addProperty("id", id);
        return sendAuth(Actions.DELETE_CATEGORY, data);
    }

    
    public List<LocationDTO> getLocations() {
        Response resp = sendAuth(Actions.GET_LOCATIONS, null);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<LocationDTO>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public Response createLocation(LocationDTO l) {
        return sendAuth(Actions.CREATE_LOCATION, gson.toJsonTree(l).getAsJsonObject());
    }

    public Response updateLocation(LocationDTO l) {
        return sendAuth(Actions.UPDATE_LOCATION, gson.toJsonTree(l).getAsJsonObject());
    }

    public Response deleteLocation(int id) {
        JsonObject data = new JsonObject(); data.addProperty("id", id);
        return sendAuth(Actions.DELETE_LOCATION, data);
    }

    
    public Response getAssignments(String keyword, String filter, int page, int pageSize) {
        Request req = authRequest(Actions.GET_ASSIGNMENTS);
        req.setKeyword(keyword); req.setFilter(filter);
        req.setPage(page); req.setPageSize(pageSize);
        return conn.send(req);
    }

    public List<AssignmentDTO> getAssignmentList(String keyword, String filter, int page, int pageSize) {
        Response resp = getAssignments(keyword, filter, page, pageSize);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<AssignmentDTO>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public Response createAssignment(AssignmentDTO a) {
        return sendAuth(Actions.CREATE_ASSIGNMENT, gson.toJsonTree(a).getAsJsonObject());
    }

    public Response returnDevice(int assignmentId, int deviceId, String returnedDate) {
        JsonObject data = new JsonObject();
        data.addProperty("id", assignmentId);
        data.addProperty("deviceId", deviceId);
        data.addProperty("returnedDate", returnedDate);
        return sendAuth(Actions.RETURN_DEVICE, data);
    }

    public Response deleteAssignment(int id) {
        JsonObject data = new JsonObject(); data.addProperty("id", id);
        return sendAuth(Actions.DELETE_ASSIGNMENT, data);
    }

   
    public Response getUsers(String keyword, int page, int pageSize) {
        Request req = authRequest(Actions.GET_USERS);
        req.setKeyword(keyword); req.setPage(page); req.setPageSize(pageSize);
        return conn.send(req);
    }

    public List<UserDTO> getUserList(String keyword, int page, int pageSize) {
        Response resp = getUsers(keyword, page, pageSize);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<UserDTO>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public Response createUser(UserDTO u, String password) {
        JsonObject data = gson.toJsonTree(u).getAsJsonObject();
        data.addProperty("password", password);
        return sendAuth(Actions.CREATE_USER, data);
    }

    public Response updateUser(UserDTO u) {
        return sendAuth(Actions.UPDATE_USER, gson.toJsonTree(u).getAsJsonObject());
    }

    public Response deleteUser(int id) {
        JsonObject data = new JsonObject(); data.addProperty("id", id);
        return sendAuth(Actions.DELETE_USER, data);
    }

    // ---- Approvals ----
    public Response getApprovals(String keyword, String filter, int page, int pageSize) {
        Request req = authRequest(Actions.GET_APPROVALS);
        req.setKeyword(keyword); req.setFilter(filter);
        req.setPage(page); req.setPageSize(pageSize);
        return conn.send(req);
    }

    public List<ApprovalDTO> getApprovalList(String keyword, String filter, int page, int pageSize) {
        Response resp = getApprovals(keyword, filter, page, pageSize);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<ApprovalDTO>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getApprovalTypes() {
        Response resp = sendAuth(Actions.GET_APPROVAL_TYPES, null);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<Map<String, Object>>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public Response createApproval(ApprovalDTO a) {
        return sendAuth(Actions.CREATE_APPROVAL, gson.toJsonTree(a).getAsJsonObject());
    }

    public Response processApproval(int id, String status, String comments) {
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        data.addProperty("status", status);
        if (comments != null) data.addProperty("comments", comments);
        return sendAuth(Actions.PROCESS_APPROVAL, data);
    }

    public Response markApprovalImported(int id) {
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        return sendAuth(Actions.MARK_APPROVAL_IMPORTED, data);
    }

    // ---- Dashboard & Logs ----
    public Response getDashboard() { return sendAuth(Actions.GET_DASHBOARD, null); }

    public DashboardDTO getDashboardData() {
        Response resp = getDashboard();
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), DashboardDTO.class);
        }
        return new DashboardDTO();
    }

    public Response getLogs(String keyword, int page, int pageSize) {
        Request req = authRequest(Actions.GET_LOGS);
        req.setKeyword(keyword); req.setPage(page); req.setPageSize(pageSize);
        return conn.send(req);
    }

    public List<Map<String, Object>> getLogList(String keyword, int page, int pageSize) {
        Response resp = getLogs(keyword, page, pageSize);
        if (resp.isSuccess() && resp.getData() != null) {
            return gson.fromJson(resp.getData(), new TypeToken<List<Map<String, Object>>>(){}.getType());
        }
        return new ArrayList<>();
    }

    // ---- Helpers ----
    private Response sendAuth(String action, JsonObject data) {
        Request req = authRequest(action);
        req.setData(data);
        return conn.send(req);
    }

    private Request authRequest(String action) {
        Request req = new Request(action);
        req.setToken(token);
        return req;
    }

    public UserDTO getCurrentUser() { return currentUser; }
    public boolean isAdmin() { return currentUser != null && currentUser.isAdmin(); }
    public String getToken() { return token; }
}
