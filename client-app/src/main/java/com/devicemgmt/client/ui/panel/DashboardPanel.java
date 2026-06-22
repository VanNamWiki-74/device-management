package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.DashboardDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class DashboardPanel extends VBox {
    private final ClientService svc;

    public DashboardPanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        Label title = UIHelper.titleLabel("📊  Dashboard - Tổng quan hệ thống");
        getChildren().add(title);

        Label loading = new Label("Đang tải dữ liệu...");
        loading.setStyle("-fx-text-fill: " + Styles.TEXT_SECONDARY + ";");
        getChildren().add(loading);

        new Thread(() -> {
            DashboardDTO data = svc.getDashboardData();
            Platform.runLater(() -> {
                getChildren().remove(loading);
                renderDashboard(data);
            });
        }).start();
    }

    private void renderDashboard(DashboardDTO data) {
        // Stats cards row
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard("💻 Tổng thiết bị",   String.valueOf(data.getTotalDevices()),     "#2563EB"),
            statCard("✅ Sẵn sàng",          String.valueOf(data.getAvailableDevices()), "#16A34A"),
            statCard("🔄 Đang dùng",         String.valueOf(data.getInUseDevices()),     "#D97706"),
            statCard("🔧 Bảo trì",           String.valueOf(data.getMaintenanceDevices()), "#EF4444"),
            statCard("📋 Phân công hiện tại",String.valueOf(data.getActiveAssignments()),"#7C3AED")
        );
        statsRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // Second row
        HBox statsRow2 = new HBox(16);
        statsRow2.getChildren().addAll(
            statCard("📁 Danh mục",    String.valueOf(data.getTotalCategories()), "#0891B2"),
            statCard("📍 Vị trí",      String.valueOf(data.getTotalLocations()),  "#059669"),
            statCard("👥 Người dùng",  String.valueOf(data.getTotalUsers()),      "#DC2626"),
            statCard("❌ Hỏng",        String.valueOf(data.getBrokenDevices()),    "#6B7280"),
            statCard("🗑 Thanh lý",    String.valueOf(data.getDisposedDevices()),  "#78716C")
        );
        statsRow2.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // Content area - two columns
        HBox contentArea = new HBox(20);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        // Devices by category
        VBox catBox = new VBox(12);
        catBox.setStyle(Styles.CARD);
        catBox.setPrefWidth(400);
        Label catTitle = new Label("📁 Thiết bị theo danh mục");
        catTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");
        catBox.getChildren().add(catTitle);

        List<Map<String, Object>> byCategory = data.getDevicesByCategory();
        if (byCategory != null) {
            for (Map<String, Object> cat : byCategory) {
                String name = String.valueOf(cat.get("name"));
                int count = ((Number) cat.get("count")).intValue();
                int total = Math.max(data.getTotalDevices(), 1);
                double pct = (double) count / total;

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label nameLbl = new Label(name);
                nameLbl.setPrefWidth(150);
                nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");

                StackPane barBg = new StackPane();
                barBg.setPrefHeight(14);
                HBox.setHgrow(barBg, Priority.ALWAYS);
                barBg.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 7;");
                StackPane bar = new StackPane();
                bar.setPrefWidth(pct * 180);
                bar.setPrefHeight(14);
                bar.setStyle("-fx-background-color: " + Styles.PRIMARY + "; -fx-background-radius: 7;");
                barBg.getChildren().add(bar);
                StackPane.setAlignment(bar, Pos.CENTER_LEFT);

                Label countLbl = new Label(String.valueOf(count));
                countLbl.setPrefWidth(30);
                countLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");
                row.getChildren().addAll(nameLbl, barBg, countLbl);
                catBox.getChildren().add(row);
            }
        }

        // Recent logs
        VBox logBox = new VBox(12);
        logBox.setStyle(Styles.CARD);
        HBox.setHgrow(logBox, Priority.ALWAYS);
        Label logTitle = new Label("📜 Hoạt động gần đây");
        logTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");
        logBox.getChildren().add(logTitle);

        List<Map<String, Object>> logs = data.getRecentLogs();
        if (logs != null) {
            for (Map<String, Object> log : logs) {
                HBox logRow = new HBox(10);
                logRow.setAlignment(Pos.CENTER_LEFT);
                logRow.setPadding(new Insets(6, 0, 6, 0));
                logRow.setStyle("-fx-border-color: transparent transparent " + Styles.BORDER_COLOR + " transparent; -fx-border-width: 1;");

                String result = String.valueOf(log.get("result"));
                String dot = "SUCCESS".equals(result) ? "🟢" : "🔴";
                Label dotLbl = new Label(dot);
                Label actionLbl = new Label(String.valueOf(log.get("action")));
                actionLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");
                actionLbl.setPrefWidth(160);
                Label userLbl = new Label(String.valueOf(log.get("username")));
                userLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");
                userLbl.setPrefWidth(80);
                String ts = String.valueOf(log.get("createdAt"));
                Label timeLbl = new Label(ts.length() > 16 ? ts.substring(0, 16) : ts);
                timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");

                logRow.getChildren().addAll(dotLbl, actionLbl, userLbl, timeLbl);
                logBox.getChildren().add(logRow);
            }
        }

        contentArea.getChildren().addAll(catBox, logBox);
        getChildren().addAll(statsRow, statsRow2, contentArea);
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.setStyle(Styles.CARD + "-fx-border-left-color: " + color + "; -fx-border-left-width: 4;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");

        card.getChildren().addAll(valLbl, nameLbl);
        return card;
    }
}
