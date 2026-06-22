package com.devicemgmt.client.ui.dialog;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.common.dto.CategoryDTO;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.Response;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class ApprovalRequestDialog {

    public static ApprovalDTO show(ClientService svc, Stage owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tạo yêu cầu phê duyệt");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(460);

        List<Map<String, Object>> types = svc.getApprovalTypes();
        if (types.isEmpty()) {
            UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi",
                "Không lấy được danh sách loại yêu cầu từ server.\nKiểm tra lại bảng approvals_types trong database hoặc khởi động lại server.");
            return null;
        }

        ComboBox<Map<String, Object>> typeBox = new ComboBox<>();
        typeBox.setStyle(Styles.COMBO_BOX);
        typeBox.setPrefWidth(Double.MAX_VALUE);
        typeBox.setPromptText("Chọn loại yêu cầu");
        typeBox.getItems().addAll(types);
        typeBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.valueOf(item.get("name")));
            }
        });
        typeBox.setButtonCell(typeBox.getCellFactory().call(null));

        // Danh mục thiết bị (dùng chung cho cả 2 luồng)
        List<CategoryDTO> categories = svc.getCategories();
        ComboBox<CategoryDTO> categoryBox = new ComboBox<>();
        categoryBox.setStyle(Styles.COMBO_BOX);
        categoryBox.setPrefWidth(Double.MAX_VALUE);
        categoryBox.setPromptText("Chọn danh mục thiết bị");
        categoryBox.getItems().addAll(categories);

        // Thiết bị mà user đang sử dụng (DevicePanel đã tự lọc theo user ở server)
        List<DeviceDTO> myDevices = svc.getDeviceList(null, null, 1, 200);
        ComboBox<DeviceDTO> deviceBox = new ComboBox<>();
        deviceBox.setStyle(Styles.COMBO_BOX);
        deviceBox.setPrefWidth(Double.MAX_VALUE);
        deviceBox.setPromptText("Chọn thiết bị đang sử dụng");
        deviceBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DeviceDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "[" + item.getCode() + "] " + item.getName());
            }
        });
        deviceBox.setButtonCell(deviceBox.getCellFactory().call(null));

        TextArea descField = UIHelper.textArea("Mô tả / lý do yêu cầu", 4);
        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: " + Styles.DANGER + ";");

        VBox categoryRow = UIHelper.formRow("Danh mục thiết bị *", categoryBox);
        VBox deviceRow = UIHelper.formRow("Thiết bị *", deviceBox);
        categoryRow.setVisible(false); categoryRow.setManaged(false);
        deviceRow.setVisible(false); deviceRow.setManaged(false);

        // Khi chọn danh mục: với luồng Sửa chữa/Thanh lý thì lọc lại thiết bị của user theo danh mục đã chọn
        categoryBox.setOnAction(e -> {
            CategoryDTO cat = categoryBox.getValue();
            deviceBox.getItems().clear();
            deviceBox.setValue(null);
            if (cat != null) {
                for (DeviceDTO d : myDevices) {
                    if (d.getCategoryId() == cat.getId()) deviceBox.getItems().add(d);
                }
            }
        });

        typeBox.setOnAction(e -> {
            Map<String, Object> t = typeBox.getValue();
            String name = t == null ? "" : String.valueOf(t.get("name"));
            boolean needsDevice = "SỬA CHỮA".equals(name) || "THANH LÝ".equals(name);
            boolean isNew = "CẤP MỚI".equals(name);
            categoryRow.setVisible(needsDevice || isNew);
            categoryRow.setManaged(needsDevice || isNew);
            deviceRow.setVisible(needsDevice);
            deviceRow.setManaged(needsDevice);
            categoryBox.setValue(null);
            deviceBox.getItems().clear();
            deviceBox.setValue(null);
        });

        VBox content = new VBox(12,
            UIHelper.formRow("Loại yêu cầu *", typeBox),
            categoryRow,
            deviceRow,
            UIHelper.formRow("Mô tả / lý do *", descField),
            errLbl);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Gửi yêu cầu");
        okBtn.setStyle(Styles.BTN_SUCCESS);

        final ApprovalDTO[] result = {null};
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            Map<String, Object> t = typeBox.getValue();
            if (t == null) { errLbl.setText("Vui lòng chọn loại yêu cầu."); e.consume(); return; }
            String typeName = String.valueOf(t.get("name"));
            boolean needsDevice = "SỬA CHỮA".equals(typeName) || "THANH LÝ".equals(typeName);
            boolean isNew = "CẤP MỚI".equals(typeName);
            if (isNew && categoryBox.getValue() == null) {
                errLbl.setText("Vui lòng chọn danh mục thiết bị cần cấp mới."); e.consume(); return;
            }
            if (needsDevice && deviceBox.getValue() == null) {
                errLbl.setText("Vui lòng chọn thiết bị (chọn danh mục trước để lọc danh sách)."); e.consume(); return;
            }
            if (descField.getText().isBlank()) { errLbl.setText("Vui lòng nhập mô tả/lý do."); e.consume(); return; }

            ApprovalDTO dto = new ApprovalDTO();
            dto.setApprovalTypeId(((Number) t.get("id")).intValue());
            if (needsDevice) {
                dto.setDeviceId(deviceBox.getValue().getId());
            } else if (isNew) {
                dto.setCategoryId(categoryBox.getValue().getId());
            }
            dto.setDescription(descField.getText().trim());

            Response resp = svc.createApproval(dto);
            if (resp.isSuccess()) {
                UIHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", resp.getMessage());
                result[0] = dto;
            } else {
                errLbl.setText(resp.getMessage());
                e.consume();
            }
        });

        dialog.showAndWait();
        return result[0];
    }
}
