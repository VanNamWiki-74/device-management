package com.devicemgmt.client.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.panel.ApprovalPanel;
import com.devicemgmt.client.ui.panel.AssignmentPanel;
import com.devicemgmt.client.ui.panel.CategoryPanel;
import com.devicemgmt.client.ui.panel.DashboardPanel;
import com.devicemgmt.client.ui.panel.DevicePanel;
import com.devicemgmt.client.ui.panel.LocationPanel;
import com.devicemgmt.client.ui.panel.LogPanel;
import com.devicemgmt.client.ui.panel.UserPanel;
import com.devicemgmt.common.dto.UserDTO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow {

    public static void show(Stage stage) {
        ClientService svc = ClientService.getInstance();
        UserDTO user = svc.getCurrentUser();

        BorderPane root = new BorderPane();

        // ---- Sidebar ----
        VBox sidebar = buildSidebar(root, user, stage);
        root.setLeft(sidebar);

        // ---- Header ----
        HBox header = buildHeader(user, stage);
        root.setTop(header);

        // ---- Default panel ----
        root.setCenter(new DashboardPanel(svc));

        Scene scene = new Scene(root, 1280, 780);
        stage.setScene(scene);
        stage.setTitle("Quản lý thiết bị văn phòng — " + user.getFullName());
        stage.setResizable(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.show();
    }

    private static HBox buildHeader(UserDTO user, Stage stage) {
        HBox header = new HBox();
        header.setPadding(new Insets(12, 24, 12, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: " + Styles.BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

        Label appName = new Label("Quản lý thiết bị văn phòng");
        appName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label roleLabel = new Label(user.isAdmin() ? "👑 Admin" : "👤 User");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (user.isAdmin() ? Styles.PRIMARY : Styles.TEXT_SECONDARY) + ";");

        Label userLabel = new Label(user.getFullName());
        userLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");

        header.getChildren().addAll(appName, spacer, roleLabel, new Label("  |  "), userLabel);
        return header;
    }

    private static VBox buildSidebar(BorderPane root, UserDTO user, Stage stage) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + Styles.SIDEBAR_BG + ";");

        // App branding
        VBox brand = new VBox(4);
        brand.setPadding(new Insets(20, 16, 20, 16));
        brand.setStyle("-fx-background-color: #0F172A;");
        Label icon = new Label("🖥 DevMgmt");
        icon.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        brand.getChildren().add(icon);
        sidebar.getChildren().add(brand);

        // Menu items: label -> panel factory
        Map<String, java.util.function.Supplier<javafx.scene.Node>> menus = new LinkedHashMap<>();
        if (user.isAdmin()) {
            menus.put("📊  Dashboard",      () -> new DashboardPanel(ClientService.getInstance()));
            menus.put("👥  Người dùng", () -> new UserPanel(ClientService.getInstance()));
            menus.put("📜  Nhật ký",    () -> new LogPanel(ClientService.getInstance()));
            menus.put("📍  Vị trí",         () -> new LocationPanel(ClientService.getInstance()));
            menus.put("📋  Phân công",      () -> new AssignmentPanel(ClientService.getInstance()));


        }

        
        menus.put("💻  Thiết bị",       () -> new DevicePanel(ClientService.getInstance()));
        menus.put("📁  Danh mục",       () -> new CategoryPanel(ClientService.getInstance()));
        menus.put("📋  Phê duyệt",      () -> new ApprovalPanel(ClientService.getInstance()));
        
        

        ToggleGroup group = new ToggleGroup();
        boolean[] first = {true};

        for (Map.Entry<String, java.util.function.Supplier<javafx.scene.Node>> entry : menus.entrySet()) {
            ToggleButton btn = new ToggleButton(entry.getKey());
            btn.setToggleGroup(group);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(12, 20, 12, 20));
            btn.setStyle(sidebarBtnStyle(false));

            btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                btn.setStyle(sidebarBtnStyle(isSelected));
            });

            btn.setOnAction(e -> root.setCenter(entry.getValue().get()));

            sidebar.getChildren().add(btn);

            if (first[0]) {
                btn.setSelected(true);
                first[0] = false;
            }
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Logout
        Button logoutBtn = new Button("🚪  Đăng xuất");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setPadding(new Insets(12, 20, 12, 20));
        logoutBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #94A3B8;
            -fx-font-size: 13px;
            -fx-cursor: hand;
            -fx-border-color: transparent;
            """);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutBtn.getStyle().replace("transparent", "#334155")));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(logoutBtn.getStyle().replace("#334155", "transparent")));

        logoutBtn.setOnAction(e -> {
            if (UIHelper.showConfirm("Đăng xuất", "Bạn có chắc muốn đăng xuất không?")) {
                ClientService.getInstance().logout();
                LoginWindow.show(stage);
            }
        });

        // Change password
        Button changePwdBtn = new Button("🔑  Đổi mật khẩu");
        changePwdBtn.setMaxWidth(Double.MAX_VALUE);
        changePwdBtn.setAlignment(Pos.CENTER_LEFT);
        changePwdBtn.setPadding(new Insets(10, 20, 10, 20));
        changePwdBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #94A3B8;
            -fx-font-size: 13px;
            -fx-cursor: hand;
            -fx-border-color: transparent;
            """);
        changePwdBtn.setOnAction(e -> showChangePasswordDialog(stage));

        sidebar.getChildren().addAll(changePwdBtn, logoutBtn);

        return sidebar;
    }

    private static String sidebarBtnStyle(boolean selected) {
        if (selected) {
            return """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-alignment: CENTER_LEFT;
                -fx-cursor: hand;
                -fx-border-color: transparent;
                """.formatted(Styles.SIDEBAR_ACTIVE);
                    }
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #CBD5E1;
            -fx-font-size: 13px;
            -fx-alignment: CENTER_LEFT;
            -fx-cursor: hand;
            -fx-border-color: transparent;
            """;
    }

    private static void showChangePasswordDialog(Stage owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField oldPwd  = UIHelper.passwordField("Mật khẩu hiện tại");
        PasswordField newPwd  = UIHelper.passwordField("Mật khẩu mới (tối thiểu 6 ký tự)");
        PasswordField confirm = UIHelper.passwordField("Xác nhận mật khẩu mới");
        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: " + Styles.DANGER + ";");

        VBox content = new VBox(10,
            UIHelper.formRow("Mật khẩu hiện tại", oldPwd),
            UIHelper.formRow("Mật khẩu mới", newPwd),
            UIHelper.formRow("Xác nhận", confirm),
            errLbl);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Đổi mật khẩu");
        okBtn.setStyle(Styles.BTN_PRIMARY);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (!newPwd.getText().equals(confirm.getText())) {
                errLbl.setText("Mật khẩu xác nhận không khớp.");
                e.consume();
                return;
            }
            if (newPwd.getText().length() < 6) {
                errLbl.setText("Mật khẩu mới phải có ít nhất 6 ký tự.");
                e.consume();
                return;
            }
            var resp = ClientService.getInstance().changePassword(oldPwd.getText(), newPwd.getText());
            if (!resp.isSuccess()) {
                errLbl.setText(resp.getMessage());
                e.consume();
            }
        });

        dialog.showAndWait();
    }
}
