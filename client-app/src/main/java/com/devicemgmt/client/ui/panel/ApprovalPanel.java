package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.client.ui.dialog.ApprovalRequestDialog;
import com.devicemgmt.client.ui.dialog.AssignmentDialog;
import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.common.dto.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ApprovalPanel extends VBox {
    private final ClientService svc;
    private TableView<ApprovalDTO> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Label totalLabel;
    private Label pageLabel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 20;

    public ApprovalPanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(16);
        buildUI();
    }

    private void buildUI() {
        boolean isAdmin = svc.isAdmin();

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = UIHelper.titleLabel("📋  Phê duyệt");
        totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        titleRow.getChildren().addAll(title, sp, totalLabel);

        searchField = UIHelper.searchField("🔍 Tìm theo mã/tên thiết bị, người yêu cầu...");
        statusFilter = UIHelper.comboBox("Trạng thái", "Tất cả", "PENDING", "APPROVED", "REJECTED");
        statusFilter.setValue(isAdmin ? "PENDING" : "Tất cả");

        Button searchBtn  = UIHelper.primaryBtn("Tìm kiếm");
        Button refreshBtn = UIHelper.secondaryBtn("↺ Làm mới");
        Button addBtn     = UIHelper.successBtn("+ Tạo yêu cầu");
        if (isAdmin) addBtn.setDisable(true);

        HBox toolbar = UIHelper.toolbar(searchField, statusFilter, searchBtn, UIHelper.spacer(), refreshBtn, addBtn);

        table = UIHelper.createTable();
        setupColumns(isAdmin);

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
            ApprovalDTO result = ApprovalRequestDialog.show(svc, (Stage) getScene().getWindow());
            if (result != null) loadData();
        });

        getChildren().addAll(titleRow, toolbar, table, pagination);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void setupColumns(boolean isAdmin) {
        TableColumn<ApprovalDTO, String> colType = new TableColumn<>("Loại");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApprovalTypeName()));
        colType.setPrefWidth(110);

        TableColumn<ApprovalDTO, String> colDevice = new TableColumn<>("Thiết bị");
        colDevice.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDeviceCode() != null ? "[" + c.getValue().getDeviceCode() + "] " + c.getValue().getDeviceName() : ""));
        colDevice.setPrefWidth(180);

        TableColumn<ApprovalDTO, String> colDesc = new TableColumn<>("Mô tả / lý do");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colDesc.setPrefWidth(220);

        TableColumn<ApprovalDTO, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                ApprovalDTO a = getTableView().getItems().get(getIndex());
                Label lbl = new Label(item);
                lbl.setStyle(Styles.statusBadge(a.getStatus()));
                setGraphic(lbl);
            }
        });
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colType, colDevice, colDesc);

        if (isAdmin) {
            TableColumn<ApprovalDTO, String> colRequester = new TableColumn<>("Người yêu cầu");
            colRequester.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRequesterName()));
            colRequester.setPrefWidth(150);
            table.getColumns().add(colRequester);
        }

        table.getColumns().add(colStatus);

        TableColumn<ApprovalDTO, String> colComments = new TableColumn<>("Ghi chú admin");
        colComments.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComments()));
        colComments.setPrefWidth(160);
        table.getColumns().add(colComments);

        if (isAdmin) {
            TableColumn<ApprovalDTO, String> colActions = new TableColumn<>("Thao tác");
            colActions.setCellFactory(col -> new TableCell<>() {
                private final Button approveBtn = UIHelper.successBtn("Duyệt");
                private final Button rejectBtn  = UIHelper.dangerBtn("Từ chối");
                private final Button importBtn  = UIHelper.primaryBtn("Import → Phân công");
                {
                    approveBtn.setPadding(new Insets(4, 8, 4, 8));
                    rejectBtn.setPadding(new Insets(4, 8, 4, 8));
                    importBtn.setPadding(new Insets(4, 8, 4, 8));

                    approveBtn.setOnAction(e -> {
                        ApprovalDTO a = getTableView().getItems().get(getIndex());
                        Response resp = svc.processApproval(a.getId(), "APPROVED", null);
                        UIHelper.showAlert(resp.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            resp.isSuccess() ? "Thành công" : "Lỗi", resp.getMessage());
                        if (resp.isSuccess()) loadData();
                    });

                    rejectBtn.setOnAction(e -> {
                        ApprovalDTO a = getTableView().getItems().get(getIndex());
                        TextInputDialog dlg = new TextInputDialog();
                        dlg.setTitle("Từ chối yêu cầu");
                        dlg.setHeaderText(null);
                        dlg.setContentText("Lý do từ chối:");
                        Optional<String> reason = dlg.showAndWait();
                        if (reason.isPresent()) {
                            Response resp = svc.processApproval(a.getId(), "REJECTED", reason.get());
                            UIHelper.showAlert(resp.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                                resp.isSuccess() ? "Thành công" : "Lỗi", resp.getMessage());
                            if (resp.isSuccess()) loadData();
                        }
                    });

                    importBtn.setOnAction(e -> {
                        ApprovalDTO a = getTableView().getItems().get(getIndex());
                        AssignmentDTO result = AssignmentDialog.show(svc, (Stage) getScene().getWindow(), a);
                        if (result != null) {
                            svc.markApprovalImported(a.getId());
                            loadData();
                        }
                    });
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    ApprovalDTO a = getTableView().getItems().get(getIndex());
                    boolean pending = "PENDING".equals(a.getStatus());
                    boolean canImport = "APPROVED".equals(a.getStatus()) && !a.isImported();
                    approveBtn.setVisible(pending); approveBtn.setManaged(pending);
                    rejectBtn.setVisible(pending); rejectBtn.setManaged(pending);
                    importBtn.setVisible(canImport); importBtn.setManaged(canImport);
                    HBox box = new HBox(6, approveBtn, rejectBtn, importBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            });
            colActions.setPrefWidth(260);
            table.getColumns().add(colActions);
        }
    }

    private void loadData() {
        String kw = searchField.getText().trim();
        String filter = "Tất cả".equals(statusFilter.getValue()) ? null : statusFilter.getValue();
        table.getItems().clear();

        new Thread(() -> {
            Response resp = svc.getApprovals(kw, filter, currentPage, PAGE_SIZE);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    List<ApprovalDTO> list = new Gson().fromJson(resp.getData(),
                        new TypeToken<List<ApprovalDTO>>(){}.getType());
                    table.getItems().setAll(list);
                    totalPages = Math.max(1, resp.getTotalPages());
                    totalLabel.setText("Tổng: " + resp.getTotalCount() + " yêu cầu");
                    pageLabel.setText("Trang " + currentPage + " / " + totalPages + " (" + resp.getTotalCount() + " bản ghi)");
                }
            });
        }).start();
    }
}
