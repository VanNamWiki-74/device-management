package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class LogPanel extends VBox {
    private final ClientService svc;
    private TableView<Map<String, Object>> table;
    private TextField searchField;
    private Label pageLabel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 30;

    public LogPanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(16);
        buildUI();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        Label title = UIHelper.titleLabel("📜  Nhật ký hệ thống");

        searchField = UIHelper.searchField("🔍 Tìm theo user, hành động...");
        Button searchBtn  = UIHelper.primaryBtn("Tìm kiếm");
        Button refreshBtn = UIHelper.secondaryBtn("↺ Làm mới");
        HBox toolbar = UIHelper.toolbar(searchField, searchBtn, UIHelper.spacer(), refreshBtn);

        table = new TableView<>();
        table.setStyle(Styles.TABLE_VIEW);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Map<String, Object>, String> colTime = new TableColumn<>("Thời gian");
        colTime.setCellValueFactory(c -> {
            String ts = String.valueOf(c.getValue().get("createdAt"));
            return new SimpleStringProperty(ts.length() > 19 ? ts.substring(0, 19) : ts);
        });
        colTime.setPrefWidth(160);

        TableColumn<Map<String, Object>, String> colUser = new TableColumn<>("Người dùng");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("username"))));
        colUser.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Hành động");
        colAction.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("action"))));
        colAction.setPrefWidth(160);

        TableColumn<Map<String, Object>, String> colTarget = new TableColumn<>("Đối tượng");
        colTarget.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("targetInfo", ""))));
        colTarget.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> colResult = new TableColumn<>("Kết quả");
        colResult.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("result"))));
        colResult.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle(Styles.statusBadge("SUCCESS".equals(item) ? "AVAILABLE" : "BROKEN"));
                setGraphic(lbl);
            }
        });
        colResult.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> colDetail = new TableColumn<>("Chi tiết");
        colDetail.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("detail", ""))));
        colDetail.setPrefWidth(200);

        table.getColumns().addAll(colTime, colUser, colAction, colTarget, colResult, colDetail);

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
        refreshBtn.setOnAction(e -> loadData());

        getChildren().addAll(title, toolbar, table, pagination);
        loadData();
    }

    private void loadData() {
        String kw = searchField.getText().trim();
        new Thread(() -> {
            Response resp = svc.getLogs(kw, currentPage, PAGE_SIZE);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    List<Map<String, Object>> list = new Gson().fromJson(resp.getData(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                    table.getItems().setAll(list);
                    totalPages = Math.max(1, resp.getTotalPages());
                    pageLabel.setText("Trang " + currentPage + " / " + totalPages + " (" + resp.getTotalCount() + " bản ghi)");
                }
            });
        }).start();
    }
}
