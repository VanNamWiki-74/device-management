package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.client.ui.dialog.AssignmentDialog;
import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class AssignmentPanel extends VBox {
    private final ClientService svc;
    private TableView<AssignmentDTO> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Label totalLabel;
    private Label pageLabel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 20;

    public AssignmentPanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(16);
        buildUI();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = UIHelper.titleLabel("📋  Phân công thiết bị");
        totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        titleRow.getChildren().addAll(title, sp, totalLabel);

        searchField  = UIHelper.searchField("🔍 Tìm người nhận, mã thiết bị...");
        statusFilter = UIHelper.comboBox("Trạng thái", "Tất cả", "ACTIVE", "RETURNED");
        statusFilter.setValue("Tất cả");

        Button searchBtn  = UIHelper.primaryBtn("Tìm kiếm");
        Button addBtn     = UIHelper.successBtn("+ Phân công mới");
        Button refreshBtn = UIHelper.secondaryBtn("↺ Làm mới");

        HBox toolbar = UIHelper.toolbar(searchField, statusFilter, searchBtn, UIHelper.spacer(), refreshBtn, addBtn);

        table = UIHelper.createTable();

        TableColumn<AssignmentDTO, String> colDevice = new TableColumn<>("Mã thiết bị");
        colDevice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeviceCode()));
        colDevice.setPrefWidth(110);

        TableColumn<AssignmentDTO, String> colDevName = new TableColumn<>("Tên thiết bị");
        colDevName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeviceName()));
        colDevName.setPrefWidth(200);

        TableColumn<AssignmentDTO, String> colAssignee = new TableColumn<>("Người nhận");
        colAssignee.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedTo()));
        colAssignee.setPrefWidth(150);

        TableColumn<AssignmentDTO, String> colDept = new TableColumn<>("Phòng ban");
        colDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartment()));
        colDept.setPrefWidth(130);

        TableColumn<AssignmentDTO, String> colDate = new TableColumn<>("Ngày cấp");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedDate()));
        colDate.setPrefWidth(110);

        TableColumn<AssignmentDTO, String> colReturn = new TableColumn<>("Ngày trả");
        colReturn.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getReturnedDate() != null ? c.getValue().getReturnedDate() : c.getValue().getExpectedReturn()));
        colReturn.setPrefWidth(110);

        TableColumn<AssignmentDTO, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                AssignmentDTO a = getTableView().getItems().get(getIndex());
                Label lbl = new Label(item);
                lbl.setStyle(Styles.statusBadge(a.getStatus()));
                setGraphic(lbl);
            }
        });
        colStatus.setPrefWidth(110);

        TableColumn<AssignmentDTO, String> colActions = new TableColumn<>("Thao tác");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button returnBtn = UIHelper.successBtn("Thu hồi");
            private final Button delBtn    = UIHelper.dangerBtn("Xóa");
            {
                returnBtn.setPadding(new Insets(4, 10, 4, 10));
                delBtn.setPadding(new Insets(4, 10, 4, 10));
                if (!svc.isAdmin()) delBtn.setDisable(true);

                returnBtn.setOnAction(e -> {
                    AssignmentDTO a = getTableView().getItems().get(getIndex());
                    if (!"ACTIVE".equals(a.getStatus())) {
                        UIHelper.showAlert(Alert.AlertType.WARNING, "Thông báo", "Thiết bị đã được trả.");
                        return;
                    }
                    if (UIHelper.showConfirm("Thu hồi thiết bị",
                        "Thu hồi thiết bị [" + a.getDeviceCode() + "] từ " + a.getAssignedTo() + "?")) {
                        Response resp = svc.returnDevice(a.getId(), a.getDeviceId(), LocalDate.now().toString());
                        UIHelper.showAlert(resp.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            resp.isSuccess() ? "Thành công" : "Lỗi", resp.getMessage());
                        if (resp.isSuccess()) loadData();
                    }
                });
                delBtn.setOnAction(e -> {
                    if (svc.isAdmin() && UIHelper.showConfirm("Xóa bản ghi", "Xóa bản ghi phân công này?")) {
                        AssignmentDTO a = getTableView().getItems().get(getIndex());
                        Response resp = svc.deleteAssignment(a.getId());
                        if (resp.isSuccess()) loadData();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                AssignmentDTO a = getTableView().getItems().get(getIndex());
                returnBtn.setDisable(!"ACTIVE".equals(a.getStatus()));
                HBox box = new HBox(6, returnBtn, delBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
        colActions.setPrefWidth(150);

        table.getColumns().addAll(colDevice, colDevName, colAssignee, colDept, colDate, colReturn, colStatus, colActions);

        // Pagination
        Button prev = UIHelper.secondaryBtn("← Trước");
        Button next = UIHelper.secondaryBtn("Sau →");
        pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: " + Styles.TEXT_SECONDARY + "; -fx-font-size: 13px;");
        prev.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        next.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });
        HBox pagination = new HBox(10, prev, pageLabel, next);
        pagination.setAlignment(Pos.CENTER);

        searchBtn.setOnAction(e -> { currentPage = 1; loadData(); });
        searchField.setOnAction(e -> { currentPage = 1; loadData(); });
        statusFilter.setOnAction(e -> { currentPage = 1; loadData(); });
        refreshBtn.setOnAction(e -> loadData());
        addBtn.setOnAction(e -> {
            AssignmentDTO result = AssignmentDialog.show(svc, (javafx.stage.Stage) getScene().getWindow());
            if (result != null) loadData();
        });

        getChildren().addAll(titleRow, toolbar, table, pagination);
        loadData();
    }

    private void loadData() {
        String kw = searchField.getText().trim();
        String filter = "Tất cả".equals(statusFilter.getValue()) ? null : statusFilter.getValue();
        table.getItems().clear();

        new Thread(() -> {
            Response resp = svc.getAssignments(kw, filter, currentPage, PAGE_SIZE);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    List<AssignmentDTO> list = new Gson().fromJson(resp.getData(),
                        new TypeToken<List<AssignmentDTO>>(){}.getType());
                    table.getItems().setAll(list);
                    totalPages = Math.max(1, resp.getTotalPages());
                    totalLabel.setText("Tổng: " + resp.getTotalCount() + " bản ghi");
                    pageLabel.setText("Trang " + currentPage + " / " + totalPages);
                }
            });
        }).start();
    }
}
