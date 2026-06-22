package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.LocationDTO;
import com.devicemgmt.common.dto.Response;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class LocationPanel extends VBox {
    private final ClientService svc;
    private TableView<LocationDTO> table;

    public LocationPanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(16);
        buildUI();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        Label title = UIHelper.titleLabel("📍  Quản lý vị trí / phòng ban");

        Button addBtn     = UIHelper.successBtn("+ Thêm vị trí");
        Button refreshBtn = UIHelper.secondaryBtn("↺ Làm mới");
        if (!svc.isAdmin()) addBtn.setDisable(true);

        HBox toolbar = UIHelper.toolbar(UIHelper.spacer(), refreshBtn, addBtn);
        table = UIHelper.createTable();

        TableColumn<LocationDTO, String> colName = new TableColumn<>("Tên vị trí / phòng");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(200);

        TableColumn<LocationDTO, String> colFloor = new TableColumn<>("Tầng");
        colFloor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFloor()));
        colFloor.setPrefWidth(100);

        TableColumn<LocationDTO, String> colDesc = new TableColumn<>("Mô tả");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colDesc.setPrefWidth(300);

        TableColumn<LocationDTO, Number> colCount = new TableColumn<>("Thiết bị");
        colCount.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDeviceCount()));
        colCount.setPrefWidth(100);

        TableColumn<LocationDTO, String> colActions = new TableColumn<>("Thao tác");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = UIHelper.primaryBtn("Sửa");
            private final Button delBtn  = UIHelper.dangerBtn("Xóa");
            {
                editBtn.setPadding(new Insets(4, 10, 4, 10));
                delBtn.setPadding(new Insets(4, 10, 4, 10));
                if (!svc.isAdmin()) { editBtn.setDisable(true); delBtn.setDisable(true); }
                editBtn.setOnAction(e -> showDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    LocationDTO l = getTableView().getItems().get(getIndex());
                    if (UIHelper.showConfirm("Xóa vị trí", "Xóa vị trí \"" + l.getName() + "\"?")) {
                        Response resp = svc.deleteLocation(l.getId());
                        UIHelper.showAlert(resp.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            resp.isSuccess() ? "Thành công" : "Lỗi", resp.getMessage());
                        if (resp.isSuccess()) loadData();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, editBtn, delBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
        colActions.setPrefWidth(140);

        table.getColumns().addAll(colName, colFloor, colDesc, colCount, colActions);
        addBtn.setOnAction(e -> showDialog(null));
        refreshBtn.setOnAction(e -> loadData());
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<LocationDTO> list = svc.getLocations();
            Platform.runLater(() -> table.getItems().setAll(list));
        }).start();
    }

    private void showDialog(LocationDTO existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm vị trí" : "Sửa vị trí");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField  = UIHelper.textField("Tên phòng / vị trí");
        TextField floorField = UIHelper.textField("Tầng (ví dụ: Tầng 1)");
        TextArea  descField  = UIHelper.textArea("Mô tả (tùy chọn)", 3);
        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: " + Styles.DANGER + ";");

        if (existing != null) {
            nameField.setText(existing.getName());
            floorField.setText(existing.getFloor());
            descField.setText(existing.getDescription());
        }

        VBox content = new VBox(12,
            UIHelper.formRow("Tên vị trí *", nameField),
            UIHelper.formRow("Tầng", floorField),
            UIHelper.formRow("Mô tả", descField),
            errLbl);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(existing == null ? "Thêm" : "Lưu");
        okBtn.setStyle(Styles.BTN_PRIMARY);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { errLbl.setText("Tên vị trí không được trống."); e.consume(); return; }
            LocationDTO dto = new LocationDTO();
            if (existing != null) dto.setId(existing.getId());
            dto.setName(name);
            dto.setFloor(floorField.getText().trim());
            dto.setDescription(descField.getText().trim());
            Response resp = existing == null ? svc.createLocation(dto) : svc.updateLocation(dto);
            if (!resp.isSuccess()) { errLbl.setText(resp.getMessage()); e.consume(); return; }
            UIHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", resp.getMessage());
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) loadData();
    }
}
