package com.itservicedesk.controller;

import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TextField searchField;

    // Detail Panel
    @FXML private Label lblProfileName;
    @FXML private Label lblProfileDept;
    @FXML private Label lblProfileEmail;
    @FXML private Label lblProfilePhone;
    @FXML private Label lblProfileDevice;

    private final UserDao userDao = new UserDao();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadUserData();
        setupSelectionListener();
    }

    private void setupTable() {
        nameCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getFullName()));
    }

    private void loadUserData() {
        userList.clear();
        List<User> users = userDao.getAllUsers();
        System.out.println("Jumlah user dari DB: " + users.size());
        userList.addAll(users);
        userTable.setItems(userList);
    }

    private void setupSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newUser) -> {
            if (newUser != null) {
                showUserDetail(newUser);
            }
        });
    }

    private void showUserDetail(User user) {
        lblProfileName.setText(user.getFullName());
        lblProfileDept.setText(user.getDepartment() + " - " + user.getTitle());
        lblProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
        lblProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "-");
        lblProfileDevice.setText(user.getAssignedDevice() != null ? user.getAssignedDevice() : "Belum ada data.");
    }

    @FXML
    private void handleDashboardNav(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout_dashboard.fxml"));
            Parent root = loader.load();
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            root.getStylesheets().add(cssPath);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah User Baru");
        dialog.setHeaderText("Masukkan data karyawan baru");

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Form fields
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Nama Lengkap");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Nomor Telepon");

        TextField titleField = new TextField();
        titleField.setPromptText("Jabatan (Title)");

        TextField deviceField = new TextField();
        deviceField.setPromptText("Assigned Device");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Staff", "Manager", "Supervisor", "Admin");
        roleBox.setValue("Staff");

        TextField departmentField = new TextField();
        departmentField.setPromptText("Departemen");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        VBox form = new VBox(10,
                new Label("Nama Lengkap"), fullNameField,
                new Label("Username"), usernameField,
                new Label("Email"), emailField,
                new Label("Phone"), phoneField,
                new Label("Jabatan"), titleField,
                new Label("Device"), deviceField,
                new Label("Role"), roleBox,
                new Label("Departemen"), departmentField,
                new Label("Password"), passwordField
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                if (fullNameField.getText().trim().isEmpty() || usernameField.getText().trim().isEmpty()) {
                    showAlert("Validasi", "Nama dan Username wajib diisi.");
                    return;
                }

                User newUser = new User(
                        fullNameField.getText().trim(),
                        usernameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        titleField.getText().trim(),
                        deviceField.getText().trim(),
                        passwordField.getText().trim().isEmpty() ? "password123" : passwordField.getText().trim(),
                        roleBox.getValue(),
                        departmentField.getText().trim()
                );

                if (userDao.insertUser(newUser)) {
                    loadUserData(); // refresh tabel
                    showAlert("Sukses", "User baru berhasil ditambahkan.");
                } else {
                    showAlert("Gagal", "Gagal menambahkan user.");
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

