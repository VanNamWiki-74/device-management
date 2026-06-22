package com.devicemgmt.server.service;

import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.dao.DeviceDAO;
import com.devicemgmt.server.dao.LogDAO;
import com.devicemgmt.server.security.TokenManager;
import com.google.gson.Gson;

import java.util.List;

public class DeviceService {
    private final DeviceDAO dao = new DeviceDAO();
    private final LogDAO logDAO = new LogDAO();
    private final Gson gson = new Gson();

    public Response getAll(Request req) {
        int page = req.getPage() < 1 ? 1 : req.getPage();
        int pageSize = req.getPageSize() < 1 ? 20 : req.getPageSize();
        String token = req.getToken();

        List<DeviceDTO> list;
        int total;
        if (TokenManager.getInstance().isAdmin(token)) {
            list = dao.findAll(req.getKeyword(), req.getFilter(), page, pageSize);
            total = dao.count(req.getKeyword(), req.getFilter());
        } else {
            int userId = TokenManager.getInstance().getUserId(token);
            list = dao.findAllForUser(userId, req.getKeyword(), req.getFilter(), page, pageSize);
            total = dao.countForUser(userId, req.getKeyword(), req.getFilter());
        }
        return Response.ok("OK", gson.toJsonTree(list), total, page, pageSize);
    }

    public Response create(Request req) {
        String token = req.getToken();
        if (!TokenManager.getInstance().isAdmin(token)) {
            return Response.error("Chỉ Admin mới có quyền thêm thiết bị.");
        }
        DeviceDTO d = gson.fromJson(req.getData(), DeviceDTO.class);

        if (d.getCode() == null || d.getCode().isBlank()) return Response.error("Mã thiết bị không được trống.");
        if (d.getName() == null || d.getName().isBlank()) return Response.error("Tên thiết bị không được trống.");
        if (dao.codeExists(d.getCode(), 0)) return Response.error("Mã thiết bị đã tồn tại: " + d.getCode());

        int id = dao.insert(d);
        if (id > 0) {
            logDAO.insert(TokenManager.getInstance().getUsername(token), "CREATE_DEVICE", "DEVICE", id, d.getCode(), "SUCCESS", d.getName());
            d.setId(id);
            return Response.ok("Thêm thiết bị thành công.", gson.toJsonTree(d));
        }
        return Response.error("Thêm thiết bị thất bại.");
    }

    public Response update(Request req) {
        String token = req.getToken();
        if (!TokenManager.getInstance().isAdmin(token)) {
            return Response.error("Chỉ Admin mới có quyền sửa thiết bị.");
        }
        DeviceDTO d = gson.fromJson(req.getData(), DeviceDTO.class);

        if (d.getCode() == null || d.getCode().isBlank()) return Response.error("Mã thiết bị không được trống.");
        if (d.getName() == null || d.getName().isBlank()) return Response.error("Tên thiết bị không được trống.");
        if (dao.codeExists(d.getCode(), d.getId())) return Response.error("Mã thiết bị đã tồn tại: " + d.getCode());

        boolean ok = dao.update(d);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(token), "UPDATE_DEVICE", "DEVICE", d.getId(), d.getCode(), "SUCCESS", d.getName());
            return Response.ok("Cập nhật thiết bị thành công.");
        }
        return Response.error("Cập nhật thiết bị thất bại.");
    }

    public Response delete(Request req) {
        String token = req.getToken();
        if (!TokenManager.getInstance().isAdmin(token)) {
            return Response.error("Chỉ Admin mới có quyền xóa thiết bị.");
        }
        int id = req.getData().get("id").getAsInt();
        DeviceDTO existing = dao.findById(id);
        if (existing == null) return Response.error("Thiết bị không tồn tại.");

        boolean ok = dao.delete(id);
        if (ok) {
            logDAO.insert(TokenManager.getInstance().getUsername(token), "DELETE_DEVICE", "DEVICE", id, existing.getCode(), "SUCCESS", existing.getName());
            return Response.ok("Xóa thiết bị thành công.");
        }
        return Response.error("Xóa thiết bị thất bại. Thiết bị có thể đang được phân công.");
    }

    public Response exportAll(Request req) {
        List<DeviceDTO> list = dao.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("Mã,Tên,Danh mục,Vị trí,Hãng,Model,Serial,Trạng thái,Ngày mua,Bảo hành,Giá,Ghi chú\n");
        for (DeviceDTO d : list) {
            csv.append(String.join(",",
                safe(d.getCode()), safe(d.getName()), safe(d.getCategoryName()), safe(d.getLocationName()),
                safe(d.getBrand()), safe(d.getModel()), safe(d.getSerialNumber()),
                safe(d.getStatus()), safe(d.getPurchaseDate()), safe(d.getWarrantyExpiry()),
                String.valueOf(d.getPurchasePrice()), safe(d.getNotes())
            )).append("\n");
        }
        logDAO.insert(TokenManager.getInstance().getUsername(req.getToken()), "EXPORT_DEVICES", "DEVICE", null, null, "SUCCESS", list.size() + " records");
        com.google.gson.JsonObject data = new com.google.gson.JsonObject();
        data.addProperty("csv", csv.toString());
        data.addProperty("count", list.size());
        return Response.ok("Xuất dữ liệu thành công.", data);
    }

    public Response importDevices(Request req) {
        String token = req.getToken();
        if (!TokenManager.getInstance().isAdmin(token)) {
            return Response.error("Chỉ Admin mới có quyền nhập dữ liệu.");
        }
        String csv = req.getData().get("csv").getAsString();
        String[] lines = csv.split("\n");
        int success = 0, failed = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",", -1);
            if (cols.length < 2) { failed++; continue; }
            try {
                DeviceDTO d = new DeviceDTO();
                d.setCode(cols[0].trim());
                d.setName(cols[1].trim());
                d.setBrand(cols.length > 4 ? cols[4].trim() : null);
                d.setModel(cols.length > 5 ? cols[5].trim() : null);
                d.setSerialNumber(cols.length > 6 ? cols[6].trim() : null);
                d.setStatus(cols.length > 7 ? cols[7].trim() : "AVAILABLE");
                d.setPurchaseDate(cols.length > 8 ? cols[8].trim() : null);
                d.setWarrantyExpiry(cols.length > 9 ? cols[9].trim() : null);
                if (!dao.codeExists(d.getCode(), 0) && !d.getCode().isBlank() && !d.getName().isBlank()) {
                    if (dao.insert(d) > 0) success++;
                    else failed++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
            }
        }
        logDAO.insert(TokenManager.getInstance().getUsername(token), "IMPORT_DEVICES", "DEVICE", null, null, "SUCCESS",
            "success=" + success + " failed=" + failed);
        return Response.ok("Nhập dữ liệu hoàn tất: " + success + " thành công, " + failed + " thất bại.");
    }

    private String safe(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
