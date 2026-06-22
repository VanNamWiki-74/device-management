package com.devicemgmt.client.ui;

import com.devicemgmt.client.ServerConnection;
import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.common.dto.Response;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LoginWindow {

    public static void show(Stage stage) {
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");

        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 50, 40, 50));
        card.setMaxWidth(400);
        card.setStyle(Styles.CARD);

       
        Label logo = new Label("🖥");
        logo.setFont(Font.font(48));
        logo.setAlignment(Pos.CENTER);

        Label title = new Label("Quản lý thiết bị\nVăn phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + Styles.TEXT_PRIMARY + "; -fx-text-alignment: center;");
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Đăng nhập để tiếp tục");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");

       
        Label serverStatus = new Label("Đang kiểm tra kết nối...");
        serverStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");

        
        TextField usernameField = UIHelper.textField("Tên đăng nhập");
        usernameField.setPrefWidth(300);

        PasswordField passwordField = UIHelper.passwordField("Mật khẩu");
        passwordField.setPrefWidth(300);

        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + Styles.DANGER + "; -fx-font-size: 13px;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

       
        Button loginBtn = UIHelper.primaryBtn("Đăng nhập");
        loginBtn.setPrefWidth(300);
        loginBtn.setPrefHeight(42);
        loginBtn.setStyle(loginBtn.getStyle() + "-fx-font-size: 15px;");

        
        Label hint = new Label("Tài khoản mặc định: admin / Admin@123");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Styles.TEXT_SECONDARY + ";");

        card.getChildren().addAll(logo, title, subtitle, serverStatus, usernameField, passwordField, errorLabel, loginBtn, hint);

        
        StackPane center = new StackPane(card);
        center.setAlignment(Pos.CENTER);
        root.setCenter(center);

        
        new Thread(() -> {
            boolean connected = ServerConnection.getInstance().connect();
            Platform.runLater(() -> {
                if (connected) {
                    serverStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Styles.SUCCESS + ";");
                    serverStatus.setText("Đã kết nối server");
                } else {
                    serverStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Styles.DANGER + ";");
                    serverStatus.setText("Không kết nối được server - kiểm tra docker compose");
                }
            });
        }).start();

       
        Runnable doLogin = () -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
                return;
            }

            loginBtn.setDisable(true);
            loginBtn.setText("Đang đăng nhập...");
            errorLabel.setText("");

            new Thread(() -> {
                Response resp = ClientService.getInstance().login(username, password);
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Đăng nhập");
                    if (resp.isSuccess()) {
                        MainWindow.show(stage);
                    } else {
                        errorLabel.setText(resp.getMessage());
                        passwordField.clear();
                    }
                });
            }).start();
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passwordField.setOnAction(e -> doLogin.run());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
