package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.client.ui.dialog.ApprovalRequestDialog;
import com.devicemgmt.client.ui.dialog.AssignmentDialog;
import com.devicemgmt.common.dto.ApprovalDTO;
import com.devicemgmt.common.dto.AssignmentDTO;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
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
    private Timeline autoRefresh;

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

        // Không có cơ chế push từ server (TCP request/response thuần) nên tự làm mới định kỳ
        // để giảm việc phải bấm tay khi có thay đổi từ phía khác (admin/user khác xử lý yêu cầu).
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(15), e -> loadData()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) autoRefresh.stop();
        });
    }

    @SuppressWarnings("unchecked")
    private void setupColumns(boolean isAdmin) {
        TableColumn<ApprovalDTO, String> colType = new TableColumn<>("Loại");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApprovalTypeName()));
        colType.setPrefWidth(110);

        TableColumn<ApprovalDTO, String> colDevice = new TableColumn<>("Thiết bị / Danh mục");
        colDevice.setCellValueFactory(c -> {
            ApprovalDTO a = c.getValue();
            if (a.getDeviceCode() != null) return new SimpleStringProperty("[" + a.getDeviceCode() + "] " + a.getDeviceName());
            if (a.getCategoryName() != null) return new SimpleStringProperty("Danh mục: " + a.getCategoryName());
            return new SimpleStringProperty("");
        });
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
                        if (resp.isSuccess()) {
                            // Cập nhật tại chỗ, không gọi lại loadData() ngay để tránh dòng vừa duyệt
                            // bị lọc mất khỏi danh sách (filter mặc định là PENDING) trước khi admin
                            // kịp bấm Import/Cập nhật trạng thái.
                            a.setStatus("APPROVED");
                            getTableView().refresh();
                        } else {
                            UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", resp.getMessage());
                        }
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
                            if (resp.isSuccess()) {
                                a.setStatus("REJECTED");
                                a.setComments(reason.get());
                                getTableView().refresh();
                            } else {
                                UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", resp.getMessage());
                            }
                        }
                    });

                    importBtn.setOnAction(e -> {
                        ApprovalDTO a = getTableView().getItems().get(getIndex());
                        if ("CẤP MỚI".equals(a.getApprovalTypeName())) {
                            // Cấp mới: chưa có thiết bị cụ thể -> mở Phân công, tự lọc thiết bị AVAILABLE theo danh mục
                            AssignmentDTO result = AssignmentDialog.show(svc, (Stage) getScene().getWindow(), a);
                            if (result != null) {
                                svc.markApprovalImported(a.getId());
                                a.setImported(true);
                                getTableView().refresh();
                            }
                        } else {
                            // Sửa chữa/Thanh lý: thiết bị đang IN_USE của user, không "phân công" lại
                            // mà cập nhật trực tiếp trạng thái thiết bị
                            String newStatus = "SỬA CHỮA".equals(a.getApprovalTypeName()) ? "MAINTENANCE" : "DISPOSED";
                            String statusLabel = "MAINTENANCE".equals(newStatus) ? "Đang bảo trì" : "Đã thanh lý";
                            List<DeviceDTO> found = svc.getDeviceList(a.getDeviceCode(), null, 1, 5);
                            DeviceDTO device = found.stream().filter(d -> d.getId() == a.getDeviceId()).findFirst().orElse(null);
                            if (device == null) {
                                UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thiết bị tương ứng.");
                                return;
                            }
                            if (!UIHelper.showConfirm("Xác nhận",
                                "Cập nhật trạng thái thiết bị [" + device.getCode() + "] " + device.getName() + " thành \"" + statusLabel + "\"?")) {
                                return;
                            }
                            device.setStatus(newStatus);
                            Response resp = svc.updateDevice(device);
                            if (resp.isSuccess()) {
                                svc.markApprovalImported(a.getId());
                                a.setImported(true);
                                getTableView().refresh();
                            } else {
                                UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", resp.getMessage());
                            }
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
                    importBtn.setText("CẤP MỚI".equals(a.getApprovalTypeName()) ? "Import → Phân công" : "Cập nhật trạng thái TB");
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
