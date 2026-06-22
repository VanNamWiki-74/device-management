package com.devicemgmt.client.ui.panel;

import com.devicemgmt.client.service.ClientService;
import com.devicemgmt.client.ui.Styles;
import com.devicemgmt.client.ui.UIHelper;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.common.dto.UserDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class UserPanel extends VBox {
    private final ClientService svc;
    private TableView<UserDTO> table;
    private TextField searchField;
    private Label pageLabel;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 20;

    public UserPanel(ClientService svc) {
        this.svc = svc;
        setStyle("-fx-background-color: " + Styles.CONTENT_BG + ";");
        setPadding(new Insets(24));
        setSpacing(16);
        buildUI();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        Label title = UIHelper.titleLabel("👥  Quản lý người dùng");

        searchField = UIHelper.searchField("🔍 Tìm theo tên, username...");
        Button searchBtn  = UIHelper.primaryBtn("Tìm kiếm");
        Button addBtn     = UIHelper.successBtn("+ Thêm tài khoản");
        Button refreshBtn = UIHelper.secondaryBtn("↺ Làm mới");
        HBox toolbar = UIHelper.toolbar(searchField, searchBtn, UIHelper.spacer(), refreshBtn, addBtn);

        table = UIHelper.createTable();

        TableColumn<UserDTO, String> colUser = new TableColumn<>("Tên đăng nhập");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colUser.setPrefWidth(140);

        TableColumn<UserDTO, String> colName = new TableColumn<>("Họ tên");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colName.setPrefWidth(180);

        TableColumn<UserDTO, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(200);

        TableColumn<UserDTO, String> colRole = new TableColumn<>("Vai trò");
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAdmin() ? "👑 Admin" : "👤 User"));
        colRole.setPrefWidth(100);

        TableColumn<UserDTO, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Hoạt động" : "Bị khóa"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                boolean active = getTableView().getItems().get(getIndex()).isActive();
                lbl.setStyle(Styles.statusBadge(active ? "ACTIVE" : "BROKEN"));
                setGraphic(lbl);
            }
        });
        colStatus.setPrefWidth(110);

        TableColumn<UserDTO, String> colCreated = new TableColumn<>("Ngày tạo");
        colCreated.setCellValueFactory(c -> {
            String ts = c.getValue().getCreatedAt();
            return new SimpleStringProperty(ts != null && ts.length() > 10 ? ts.substring(0, 10) : ts);
        });
        colCreated.setPrefWidth(110);

        TableColumn<UserDTO, String> colActions = new TableColumn<>("Thao tác");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = UIHelper.primaryBtn("Sửa");
            private final Button delBtn  = UIHelper.dangerBtn("Xóa");
            {
                editBtn.setPadding(new Insets(4, 10, 4, 10));
                delBtn.setPadding(new Insets(4, 10, 4, 10));
                editBtn.setOnAction(e -> showDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    UserDTO u = getTableView().getItems().get(getIndex());
                    if (UIHelper.showConfirm("Xóa tài khoản", "Xóa tài khoản \"" + u.getUsername() + "\"?")) {
                        Response resp = svc.deleteUser(u.getId());
                        UIHelper.showAlert(resp.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            resp.isSuccess() ? "Thành công" : "Lỗi", resp.getMessage());
                        if (resp.isSuccess()) loadData();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                UserDTO u = getTableView().getItems().get(getIndex());
                // Can't delete self
                delBtn.setDisable(u.getId() == svc.getCurrentUser().getId() || u.isAdmin());
                HBox box = new HBox(6, editBtn, delBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
        colActions.setPrefWidth(140);

        table.getColumns().addAll(colUser, colName, colEmail, colRole, colStatus, colCreated, colActions);

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
        addBtn.setOnAction(e -> showDialog(null));

        getChildren().addAll(title, toolbar, table, pagination);
        loadData();
    }

    private void loadData() {
        String kw = searchField.getText().trim();
        new Thread(() -> {
            Response resp = svc.getUsers(kw, currentPage, PAGE_SIZE);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    List<UserDTO> list = new Gson().fromJson(resp.getData(),
                        new TypeToken<List<UserDTO>>(){}.getType());
                    table.getItems().setAll(list);
                    totalPages = Math.max(1, resp.getTotalPages());
                    pageLabel.setText("Trang " + currentPage + " / " + totalPages + " (" + resp.getTotalCount() + " tài khoản)");
                }
            });
        }).start();
    }

    private void showDialog(UserDTO existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm tài khoản" : "Sửa tài khoản");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField usernameField = UIHelper.textField("Tên đăng nhập");
        TextField fullNameField = UIHelper.textField("Họ và tên");
        TextField emailField    = UIHelper.textField("Email");
        TextField phoneField    = UIHelper.textField("Số điện thoại");
        ComboBox<String> roleBox = UIHelper.comboBox("Vai trò", "USER", "ADMIN");
        PasswordField pwdField  = UIHelper.passwordField("Mật khẩu (tối thiểu 6 ký tự)");
        CheckBox activeBox      = new CheckBox("Tài khoản đang hoạt động");
        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: " + Styles.DANGER + ";");

        if (existing != null) {
            usernameField.setText(existing.getUsername());
            usernameField.setEditable(false);
            fullNameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            roleBox.setValue(existing.getRole());
            activeBox.setSelected(existing.isActive());
        } else {
            roleBox.setValue("USER");
            activeBox.setSelected(true);
        }

        VBox content = new VBox(10,
            UIHelper.formRow("Tên đăng nhập *", usernameField),
            UIHelper.formRow("Họ tên *", fullNameField),
            UIHelper.formRow("Email", emailField),
            UIHelper.formRow("Số điện thoại", phoneField),
            UIHelper.formRow("Vai trò", roleBox),
            existing == null ? UIHelper.formRow("Mật khẩu *", pwdField) : new Label(),
            activeBox,
            errLbl
        );
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(existing == null ? "Tạo tài khoản" : "Lưu");
        okBtn.setStyle(Styles.BTN_PRIMARY);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (usernameField.getText().isBlank()) { errLbl.setText("Tên đăng nhập không được trống."); e.consume(); return; }
            if (fullNameField.getText().isBlank()) { errLbl.setText("Họ tên không được trống."); e.consume(); return; }

            UserDTO dto = new UserDTO();
            if (existing != null) dto.setId(existing.getId());
            dto.setUsername(usernameField.getText().trim());
            dto.setFullName(fullNameField.getText().trim());
            dto.setEmail(emailField.getText().trim());
            dto.setPhone(phoneField.getText().trim());
            dto.setRole(roleBox.getValue());
            dto.setActive(activeBox.isSelected());

            Response resp;
            if (existing == null) {
                resp = svc.createUser(dto, pwdField.getText());
            } else {
                resp = svc.updateUser(dto);
            }
            if (!resp.isSuccess()) { errLbl.setText(resp.getMessage()); e.consume(); return; }
            UIHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", resp.getMessage());
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) loadData();
    }
}
