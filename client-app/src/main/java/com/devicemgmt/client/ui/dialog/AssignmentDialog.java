package com.devicemgmt.client.ui.dialog;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.common.dto.UserDTO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class AssignmentDialog {

    public static AssignmentDTO show(ClientService svc, Stage owner) {
        return show(svc, owner, null);
    }

    public static AssignmentDTO show(ClientService svc, Stage owner, ApprovalDTO prefillFrom) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Phân công thiết bị");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(480);

        // Load available devices
        ComboBox<DeviceDTO> deviceBox = new ComboBox<>();
        deviceBox.setStyle(Styles.COMBO_BOX);
        deviceBox.setPrefWidth(Double.MAX_VALUE);
        deviceBox.setPromptText("Chọn thiết bị (chỉ AVAILABLE)");

        List<DeviceDTO> devices = svc.getDeviceList(null, "AVAILABLE", 1, 200);
        deviceBox.getItems().addAll(devices);
        deviceBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DeviceDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "[" + item.getCode() + "] " + item.getName());
            }
        });
        deviceBox.setButtonCell(deviceBox.getCellFactory().call(null));

        // Load users (tài khoản) for proper userId linkage
        ComboBox<UserDTO> userBox = new ComboBox<>();
        userBox.setStyle(Styles.COMBO_BOX);
        userBox.setPrefWidth(Double.MAX_VALUE);
        userBox.setPromptText("Chọn người dùng");
        List<UserDTO> users = svc.getUserList(null, 1, 200);
        userBox.getItems().addAll(users);
        userBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName() + " (" + item.getUsername() + ")");
            }
        });
        userBox.setButtonCell(userBox.getCellFactory().call(null));

        TextField assigneeField = UIHelper.textField("Họ tên người nhận");
        TextField deptField     = UIHelper.textField("Phòng ban / bộ phận");
        TextField dateField     = UIHelper.textField("yyyy-MM-dd");
        dateField.setText(LocalDate.now().toString());
        TextField expectedReturn = UIHelper.textField("yyyy-MM-dd (tùy chọn)");
        TextArea notesField = UIHelper.textArea("Ghi chú", 2);
        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: " + Styles.DANGER + ";");

        userBox.setOnAction(e -> {
            if (userBox.getValue() != null) assigneeField.setText(userBox.getValue().getFullName());
        });

        if (prefillFrom != null) {
            for (DeviceDTO d : devices) {
                if (d.getId() == prefillFrom.getDeviceId()) { deviceBox.setValue(d); break; }
            }
            for (UserDTO u : users) {
                if (u.getId() == prefillFrom.getRequesterId()) {
                    userBox.setValue(u);
                    assigneeField.setText(u.getFullName());
                    break;
                }
            }
            notesField.setText(prefillFrom.getDescription());
        }

        VBox content = new VBox(12,
            UIHelper.formRow("Thiết bị *", deviceBox),
            UIHelper.formRow("Người dùng (tài khoản) *", userBox),
            UIHelper.formRow("Người nhận *", assigneeField),
            UIHelper.formRow("Phòng ban", deptField),
            UIHelper.formRow("Ngày cấp *", dateField),
            UIHelper.formRow("Dự kiến trả (tùy chọn)", expectedReturn),
            UIHelper.formRow("Ghi chú", notesField),
            errLbl);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Phân công");
        okBtn.setStyle(Styles.BTN_SUCCESS);

        final AssignmentDTO[] result = {null};
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (deviceBox.getValue() == null) { errLbl.setText("Vui lòng chọn thiết bị."); e.consume(); return; }
            if (userBox.getValue() == null) { errLbl.setText("Vui lòng chọn người dùng."); e.consume(); return; }
            if (assigneeField.getText().isBlank()) { errLbl.setText("Tên người nhận không được trống."); e.consume(); return; }
            if (dateField.getText().isBlank()) { errLbl.setText("Ngày cấp không được trống."); e.consume(); return; }

            AssignmentDTO dto = new AssignmentDTO();
            dto.setDeviceId(deviceBox.getValue().getId());
            dto.setDeviceCode(deviceBox.getValue().getCode());
            dto.setDeviceName(deviceBox.getValue().getName());
            dto.setUserId(userBox.getValue().getId());
            dto.setAssignedTo(assigneeField.getText().trim());
            dto.setDepartment(deptField.getText().trim());
            dto.setAssignedDate(dateField.getText().trim());
            dto.setExpectedReturn(expectedReturn.getText().trim());
            dto.setNotes(notesField.getText().trim());

            Response resp = svc.createAssignment(dto);
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
