package com.devicemgmt.client.ui.dialog;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.CategoryDTO;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.LocationDTO;
import com.devicemgmt.common.dto.Response;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class DeviceDialog {

    public static DeviceDTO show(DeviceDTO existing, ClientService svc, Stage owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm thiết bị mới" : "Sửa thông tin thiết bị");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(580);

        // Form fields
        TextField codeField   = UIHelper.textField("Ví dụ: MH-001, PC-002...");
        TextField nameField   = UIHelper.textField("Tên đầy đủ của thiết bị");
        TextField brandField  = UIHelper.textField("Dell, HP, Logitech...");
        TextField modelField  = UIHelper.textField("Model số");
        TextField serialField = UIHelper.textField("Số serial");
        TextField purchaseDateField = UIHelper.textField("yyyy-MM-dd");
        TextField warrantyField     = UIHelper.textField("yyyy-MM-dd");
        TextField priceField  = UIHelper.textField("Giá mua (VNĐ)");
        TextArea  notesField  = UIHelper.textArea("Ghi chú", 2);
        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: " + Styles.DANGER + ";");
        errLbl.setWrapText(true);

        // Category ComboBox
        ComboBox<CategoryDTO> catBox = new ComboBox<>();
        catBox.setStyle(Styles.COMBO_BOX);
        catBox.setPrefWidth(Double.MAX_VALUE);

        // Location ComboBox
        ComboBox<LocationDTO> locBox = new ComboBox<>();
        locBox.setStyle(Styles.COMBO_BOX);
        locBox.setPrefWidth(Double.MAX_VALUE);

        // Status ComboBox
        ComboBox<String> statusBox = UIHelper.comboBox("Trạng thái",
            "AVAILABLE", "IN_USE", "MAINTENANCE", "BROKEN", "DISPOSED");
        statusBox.setPrefWidth(Double.MAX_VALUE);
        statusBox.setValue("AVAILABLE");

        // Load categories and locations
        List<CategoryDTO> cats = svc.getCategories();
        catBox.getItems().addAll(cats);
        List<LocationDTO> locs = svc.getLocations();
        locBox.getItems().addAll(locs);

        // Populate if editing
        if (existing != null) {
            codeField.setText(existing.getCode());
            nameField.setText(existing.getName());
            brandField.setText(existing.getBrand());
            modelField.setText(existing.getModel());
            serialField.setText(existing.getSerialNumber());
            purchaseDateField.setText(existing.getPurchaseDate());
            warrantyField.setText(existing.getWarrantyExpiry());
            priceField.setText(existing.getPurchasePrice() > 0 ? String.valueOf((long) existing.getPurchasePrice()) : "");
            notesField.setText(existing.getNotes());
            statusBox.setValue(existing.getStatus() != null ? existing.getStatus() : "AVAILABLE");

            cats.stream().filter(c -> c.getId() == existing.getCategoryId()).findFirst().ifPresent(catBox::setValue);
            locs.stream().filter(l -> l.getId() == existing.getLocationId()).findFirst().ifPresent(locBox::setValue);
        }

        // Layout - 2 column grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        int row = 0;
        addRow(grid, row++, "Mã thiết bị *", codeField, "Tên thiết bị *", nameField);
        addRow(grid, row++, "Danh mục", catBox, "Vị trí", locBox);
        addRow(grid, row++, "Hãng sản xuất", brandField, "Model", modelField);
        addRow(grid, row++, "Số serial", serialField, "Trạng thái", statusBox);
        addRow(grid, row++, "Ngày mua (yyyy-MM-dd)", purchaseDateField, "Hết bảo hành", warrantyField);
        addRow(grid, row++, "Giá mua (VNĐ)", priceField, "Ghi chú", notesField);

        // Full width constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        VBox content = new VBox(0, grid, errLbl);
        errLbl.setPadding(new Insets(0, 20, 10, 20));
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(existing == null ? "Thêm thiết bị" : "Lưu thay đổi");
        okBtn.setStyle(Styles.BTN_PRIMARY);

        final DeviceDTO[] result = {null};

        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            // Validation
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();

            if (code.isEmpty()) { errLbl.setText("Mã thiết bị không được trống."); e.consume(); return; }
            if (name.isEmpty()) { errLbl.setText("Tên thiết bị không được trống."); e.consume(); return; }

            DeviceDTO dto = new DeviceDTO();
            if (existing != null) dto.setId(existing.getId());
            dto.setCode(code);
            dto.setName(name);
            if (catBox.getValue() != null) {
                dto.setCategoryId(catBox.getValue().getId());
                dto.setCategoryName(catBox.getValue().getName());
            }
            if (locBox.getValue() != null) {
                dto.setLocationId(locBox.getValue().getId());
                dto.setLocationName(locBox.getValue().getName());
            }
            dto.setBrand(brandField.getText().trim());
            dto.setModel(modelField.getText().trim());
            dto.setSerialNumber(serialField.getText().trim());
            dto.setStatus(statusBox.getValue());
            dto.setPurchaseDate(purchaseDateField.getText().trim());
            dto.setWarrantyExpiry(warrantyField.getText().trim());
            String priceStr = priceField.getText().trim();
            if (!priceStr.isEmpty()) {
                try { dto.setPurchasePrice(Double.parseDouble(priceStr.replace(",", ""))); }
                catch (NumberFormatException ex) { errLbl.setText("Giá mua phải là số."); e.consume(); return; }
            }
            dto.setNotes(notesField.getText().trim());

            Response resp = existing == null ? svc.createDevice(dto) : svc.updateDevice(dto);
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

    private static void addRow(GridPane grid, int row, String label1, javafx.scene.Node field1, String label2, javafx.scene.Node field2) {
        Label lbl1 = UIHelper.label(label1);
        Label lbl2 = UIHelper.label(label2);
        VBox box1 = new VBox(4, lbl1, field1);
        VBox box2 = new VBox(4, lbl2, field2);
        GridPane.setColumnSpan(field1, 1);
        grid.add(box1, 0, row, 2, 1);
        grid.add(box2, 2, row, 2, 1);
    }
}
