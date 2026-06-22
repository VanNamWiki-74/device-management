package com.devicemgmt.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class UIHelper {
    private UIHelper() {}

    public static Button primaryBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Styles.BTN_PRIMARY);
        return btn;
    }

    public static Button successBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Styles.BTN_SUCCESS);
        return btn;
    }

    public static Button dangerBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Styles.BTN_DANGER);
        return btn;
    }

    public static Button secondaryBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Styles.BTN_SECONDARY);
        return btn;
    }

    public static TextField textField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(Styles.TEXT_FIELD);
        return tf;
    }

    public static TextField searchField(String prompt) {
        TextField tf = textField(prompt);
        tf.setPrefWidth(250);
        return tf;
    }

    public static PasswordField passwordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(Styles.TEXT_FIELD);
        return pf;
    }

    public static TextArea textArea(String prompt, int rows) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(rows);
        ta.setWrapText(true);
        ta.setStyle(Styles.TEXT_FIELD);
        return ta;
    }

    public static ComboBox<String> comboBox(String prompt, String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(prompt);
        cb.getItems().addAll(items);
        cb.setStyle(Styles.COMBO_BOX);
        cb.setPrefWidth(200);
        return cb;
    }

    public static Label label(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(Styles.LABEL_FIELD);
        return lbl;
    }

    public static Label titleLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + Styles.TEXT_PRIMARY + ";");
        return lbl;
    }

    public static VBox formRow(String labelText, Node field) {
        VBox box = new VBox(4);
        box.getChildren().addAll(label(labelText), field);
        return box;
    }

    public static HBox toolbar(Node... nodes) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 12, 0));
        box.getChildren().addAll(nodes);
        return box;
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    public static <T> TableView<T> createTable() {
        TableView<T> table = new TableView<>();
        table.setStyle(Styles.TABLE_VIEW);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    public static <T> TableColumn<T, String> column(String title, javafx.util.Callback<TableColumn<T, String>, TableCell<T, String>> cellFactory, javafx.beans.value.ObservableValue<String> dummy) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellFactory(cellFactory);
        return col;
    }

    public static HBox paginationBar(int[] state, int totalPages, Runnable onRefresh) {
        // state[0] = current page
        Label pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: " + Styles.TEXT_SECONDARY + "; -fx-font-size: 13px;");

        Button prevBtn = secondaryBtn("← Trước");
        Button nextBtn = secondaryBtn("Sau →");

        Runnable updateLabel = () -> pageLabel.setText("Trang " + state[0] + " / " + totalPages);
        updateLabel.run();

        prevBtn.setOnAction(e -> {
            if (state[0] > 1) { state[0]--; onRefresh.run(); }
        });
        nextBtn.setOnAction(e -> {
            if (state[0] < totalPages) { state[0]++; onRefresh.run(); }
        });

        HBox box = new HBox(10, prevBtn, pageLabel, nextBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }
}
