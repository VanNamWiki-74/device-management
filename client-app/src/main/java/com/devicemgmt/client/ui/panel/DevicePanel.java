package com.devicemgmt.client.ui.panel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.client.ui.dialog.DeviceDialog;
import com.devicemgmt.common.dto.DeviceDTO;
import com.devicemgmt.common.dto.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class DevicePanel extends VBox {
    private final ClientService svc;
    private TableView<DeviceDTO> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Label totalLabel;
    private Label pageLabel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 20;

    public DevicePanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(16);
        buildUI();
    }

    private void buildUI() {
        // Title row
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = UIHelper.titleLabel("💻  Quản lý thiết bị");
        totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        titleRow.getChildren().addAll(title, sp, totalLabel);

        // Toolbar
        searchField = UIHelper.searchField("🔍 Tìm theo mã, tên, hãng...");
        statusFilter = UIHelper.comboBox("Trạng thái", "Tất cả", "AVAILABLE", "IN_USE", "MAINTENANCE", "BROKEN", "DISPOSED");
        statusFilter.setValue("Tất cả");
        if (!svc.isAdmin()) { statusFilter.setDisable(true); statusFilter.setDisable(true); }

        Button searchBtn = UIHelper.primaryBtn("Tìm kiếm");
        Button addBtn    = UIHelper.successBtn("+ Thêm thiết bị");
        Button exportBtn = UIHelper.secondaryBtn("↑ Xuất CSV");

        Button importBtn = UIHelper.secondaryBtn("↓ Nhập CSV");
        Button refreshBtn = UIHelper.secondaryBtn("↺ Làm mới");

        if (!svc.isAdmin()) { addBtn.setDisable(true); importBtn.setDisable(true); }

        HBox toolbar = UIHelper.toolbar(searchField, statusFilter, searchBtn, UIHelper.spacer(), refreshBtn, exportBtn, importBtn, addBtn);

        // Table
        table = UIHelper.createTable();
        setupColumns();

        // Pagination
        HBox pagination = buildPagination();

        // Events
        searchBtn.setOnAction(e -> { currentPage = 1; loadData(); });
        searchField.setOnAction(e -> { currentPage = 1; loadData(); });
        statusFilter.setOnAction(e -> { currentPage = 1; loadData(); });
        refreshBtn.setOnAction(e -> loadData());

        addBtn.setOnAction(e -> {
            DeviceDTO result = DeviceDialog.show(null, svc, (Stage) getScene().getWindow());
            if (result != null) loadData();
        });

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !table.getSelectionModel().isEmpty()) {
                DeviceDTO selected = table.getSelectionModel().getSelectedItem();
                DeviceDTO result = DeviceDialog.show(selected, svc, (Stage) getScene().getWindow());
                if (result != null) loadData();
            }
        });

        exportBtn.setOnAction(e -> doExport());
        importBtn.setOnAction(e -> doImport());

        getChildren().addAll(titleRow, toolbar, table, pagination);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<DeviceDTO, String> colCode = new TableColumn<>("Mã");
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
        colCode.setPrefWidth(100);

        TableColumn<DeviceDTO, String> colName = new TableColumn<>("Tên thiết bị");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(220);

        TableColumn<DeviceDTO, String> colCat = new TableColumn<>("Danh mục");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoryName()));
        colCat.setPrefWidth(130);

        TableColumn<DeviceDTO, String> colLoc = new TableColumn<>("Vị trí");
        colLoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocationName()));
        colLoc.setPrefWidth(130);

        TableColumn<DeviceDTO, String> colBrand = new TableColumn<>("Hãng");
        colBrand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand()));
        colBrand.setPrefWidth(100);

        TableColumn<DeviceDTO, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        colModel.setPrefWidth(100);

        if (svc.isAdmin()) {
            TableColumn<DeviceDTO, String> colStatus = new TableColumn<>("Trạng thái");
            colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
            colStatus.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    DeviceDTO d = getTableView().getItems().get(getIndex());
                    Label lbl = new Label(item);
                    lbl.setStyle(Styles.statusBadge(d.getStatus()));
                    setGraphic(lbl);
                }
            });
            colStatus.setPrefWidth(110);

            TableColumn<DeviceDTO, String> colWarranty = new TableColumn<>("Hết BH");
            colWarranty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarrantyExpiry()));
            colWarranty.setPrefWidth(100);

            TableColumn<DeviceDTO, String> colActions = new TableColumn<>("Thao tác");
            colActions.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = UIHelper.primaryBtn("Sửa");
                private final Button delBtn  = UIHelper.dangerBtn("Xóa");
                {
                    editBtn.setPadding(new Insets(4, 10, 4, 10));
                    delBtn.setPadding(new Insets(4, 10, 4, 10));
                    if (!svc.isAdmin()) { editBtn.setDisable(true); delBtn.setDisable(true); }
                    editBtn.setOnAction(e -> {
                        DeviceDTO d = getTableView().getItems().get(getIndex());
                        DeviceDTO result = DeviceDialog.show(d, svc, (Stage) getScene().getWindow());
                        if (result != null) loadData();
                    });
                    delBtn.setOnAction(e -> {
                        DeviceDTO d = getTableView().getItems().get(getIndex());
                        if (UIHelper.showConfirm("Xóa thiết bị", "Bạn có chắc muốn xóa thiết bị [" + d.getCode() + "] " + d.getName() + "?")) {
                            Response resp = svc.deleteDevice(d.getId());
                            if (resp.isSuccess()) { UIHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", resp.getMessage()); loadData(); }
                            else UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", resp.getMessage());
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
            colActions.setPrefWidth(130);
            table.getColumns().addAll(colCode, colName, colCat, colLoc, colBrand, colModel, colStatus, colWarranty, colActions);
        } else {
            table.getColumns().addAll(colCode, colName, colCat, colLoc, colBrand, colModel);
        }
    }

    private HBox buildPagination() {
        Button prev = UIHelper.secondaryBtn("← Trước");
        Button next = UIHelper.secondaryBtn("Sau →");
        pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: " + Styles.TEXT_SECONDARY + "; -fx-font-size: 13px;");

        prev.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        next.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        HBox box = new HBox(10, prev, pageLabel, next);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void loadData() {
        String kw = searchField.getText().trim();
        String filter = "Tất cả".equals(statusFilter.getValue()) ? null : statusFilter.getValue();
        table.getItems().clear();

        new Thread(() -> {
            Response resp = svc.getDevices(kw, filter, currentPage, PAGE_SIZE);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    List<DeviceDTO> list = new Gson().fromJson(resp.getData(),
                        new TypeToken<List<DeviceDTO>>(){}.getType());
                    table.getItems().setAll(list);
                    totalPages = Math.max(1, resp.getTotalPages());
                    totalLabel.setText("Tổng: " + resp.getTotalCount() + " thiết bị");
                    updatePagination(resp.getTotalCount());
                }
            });
        }).start();
    }

    private void updatePagination(int total) {
        pageLabel.setText("Trang " + currentPage + " / " + totalPages + " (" + total + " bản ghi)");
    }

    private void doExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fc.setInitialFileName("devices.csv");
        File file = fc.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        new Thread(() -> {
            Response resp = svc.exportDevices();
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    String csv = resp.getData().getAsJsonObject().get("csv").getAsString();
                    try (FileWriter fw = new FileWriter(file)) {
                        fw.write(csv);
                        UIHelper.showAlert(Alert.AlertType.INFORMATION, "Xuất dữ liệu", "Xuất thành công: " + file.getAbsolutePath());
                    } catch (IOException ex) {
                        UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu file: " + ex.getMessage());
                    }
                } else {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", resp.getMessage());
                }
            });
        }).start();
    }

    private void doImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = fc.showOpenDialog(getScene().getWindow());
        if (file == null) return;

        try {
            String csv = Files.readString(file.toPath());
            new Thread(() -> {
                Response resp = svc.importDevices(csv);
                Platform.runLater(() -> {
                    UIHelper.showAlert(resp.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        "Nhập dữ liệu", resp.getMessage());
                    if (resp.isSuccess()) loadData();
                });
            }).start();
        } catch (IOException e) {
            UIHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không đọc được file: " + e.getMessage());
        }
    }
}
