package com.devicemgmt.client;

import com.devicemgmt.client.ui.LoginWindow;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ClientApp extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Quản lý thiết bị văn phòng");
        stage.setMinWidth(400);
        stage.setMinHeight(300);

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception ignored) {}

        LoginWindow.show(stage);
    }

    @Override
    public void stop() {
        ServerConnection.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
