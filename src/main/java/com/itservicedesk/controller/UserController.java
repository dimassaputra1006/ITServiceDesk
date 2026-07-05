package com.itservicedesk.controller;

import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    // Bundled fallback shown whenever a user has no uploaded photo.
    // Place the actual asset at: src/main/resources/images/default-avatar.jpg
    private static final String DEFAULT_AVATAR_RESOURCE = "/images/default-avatar.jpg";

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TextField searchField;

    // Detail Panel
    @FXML private Circle profileAvatar;
    @FXML private Label lblProfileName;
    @FXML private Label lblProfileDept;
    @FXML private Label lblProfileEmail;
    @FXML private Label lblProfilePhone;
    @FXML private Label lblProfileDevice;

    private final UserDao userDao = new UserDao();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

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
        System.out.println("Users loaded from DB: " + users.size());
        userList.addAll(users);
        userTable.setItems(userList);

        // Default to showing the first employee instead of an empty placeholder
        if (!userList.isEmpty()) {
            userTable.getSelectionModel().selectFirst();
        } else {
            showEmptyProfile();
        }
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
        lblProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
        lblProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "-");
        lblProfileDevice.setText(user.getAssignedDevice() != null ? user.getAssignedDevice() : "No data yet.");
        applyAvatar(user);
    }

    // Shown only when the directory has zero employees
    private void showEmptyProfile() {
        lblProfileName.setText("Select Employee");
        lblProfileDept.setText("-");
        lblProfileEmail.setText("-");
        lblProfilePhone.setText("-");
        lblProfileDevice.setText("No data yet.");
        applyAvatar(null);
    }

    // ==================== Avatar handling ====================

    private void applyAvatar(User user) {
        Image image = loadAvatarImage(user);
        if (image != null) {
            profileAvatar.setFill(new ImagePattern(image));
        } else {
            // No uploaded photo and no default asset available yet —
            // fall back to the plain CSS circle (see .avatar-placeholder in styles.css).
            profileAvatar.setFill(null);
        }
    }

    private Image loadAvatarImage(User user) {
        String path = user != null ? user.getAvatarPath() : null;
        if (path != null && !path.isBlank()) {
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
            System.err.println("Avatar file not found, falling back to default: " + path);
        }
        return loadDefaultAvatar();
    }

    private Image loadDefaultAvatar() {
        InputStream stream = getClass().getResourceAsStream(DEFAULT_AVATAR_RESOURCE);
        if (stream == null) {
            System.err.println("Default avatar resource missing at " + DEFAULT_AVATAR_RESOURCE);
            return null;
        }
        return new Image(stream);
    }

    // ==================== Navigation ====================

    @FXML
    private void handleDashboardNav(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/ticket_dashboard.fxml"));
            Parent root = loader.load();
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            root.getStylesheets().add(cssPath);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== Add User ====================

    @FXML
    private void handleAddUser(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter the new employee's details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Form fields
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField titleField = new TextField();
        titleField.setPromptText("Job Title");

        TextField deviceField = new TextField();
        deviceField.setPromptText("Assigned Device");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Staff", "Manager", "Supervisor", "Admin");
        roleBox.setValue("Staff");

        TextField departmentField = new TextField();
        departmentField.setPromptText("Department");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Optional profile photo picker — leave unset to use the default avatar
        ImageView avatarPreview = new ImageView();
        avatarPreview.setFitWidth(72);
        avatarPreview.setFitHeight(72);
        avatarPreview.setPreserveRatio(true);

        // Array wrapper so the lambda below can assign to it (needs an effectively-final reference)
        File[] selectedAvatarFile = new File[1];

        Button choosePhotoBtn = new Button("Choose Photo (optional)");
        choosePhotoBtn.setOnAction(ev -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedAvatarFile[0] = file;
                avatarPreview.setImage(new Image(file.toURI().toString()));
            }
        });

        VBox form = new VBox(10,
                new Label("Full Name"), fullNameField,
                new Label("Username"), usernameField,
                new Label("Email"), emailField,
                new Label("Phone"), phoneField,
                new Label("Job Title"), titleField,
                new Label("Device"), deviceField,
                new Label("Role"), roleBox,
                new Label("Department"), departmentField,
                new Label("Password"), passwordField,
                new Label("Profile Photo"), choosePhotoBtn, avatarPreview
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("workstation-dialog");

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                if (fullNameField.getText().trim().isEmpty() || usernameField.getText().trim().isEmpty()) {
                    showAlert("Validation", "Full name and username are required.");
                    return;
                }

                User newUser = new User(
                        fullNameField.getText().trim(),
                        usernameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        deviceField.getText().trim(),
                        passwordField.getText().trim().isEmpty() ? "password123" : passwordField.getText().trim(),
                        roleBox.getValue(),
                        departmentField.getText().trim()
                );

                if (selectedAvatarFile[0] != null) {
                    newUser.setAvatarPath(selectedAvatarFile[0].getAbsolutePath());
                }

                if (userDao.insertUser(newUser)) {
                    loadUserData();
                    showAlert("Success", "New user added successfully.");
                } else {
                    showAlert("Failed", "Failed to add new user.");
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