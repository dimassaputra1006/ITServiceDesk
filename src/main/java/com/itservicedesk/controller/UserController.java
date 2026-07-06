package com.itservicedesk.controller;

import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.User;
import com.itservicedesk.util.AnalystSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.collections.transformation.FilteredList;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    // Bundled fallback shown whenever a user has no uploaded photo.
    // Place the actual asset at: src/main/resources/images/default-avatar.jpg
    private static final String DEFAULT_AVATAR_RESOURCE = "/images/default-avatar.jpg";

    // Where uploaded profile photos are permanently copied to, so the app
    // no longer depends on wherever the user originally picked the file
    // from (that folder could later be renamed, moved, or be on a
    // different machine entirely).
    private static final String AVATAR_STORAGE_DIR = "src/main/resources/uploads/avatars";

    private static final DateTimeFormatter PROFILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TextField searchField;

    // Detail Panel
    @FXML private Circle profileAvatar;
    @FXML private Label lblProfileName;
    @FXML private Label lblAnalystName;
    @FXML private ComboBox<String> cbProfileRole;
    @FXML private ComboBox<String> cbProfileDept;
    @FXML private Label lblAccountStatus;
    @FXML private Label lblProfileEmail;
    @FXML private Label lblProfileUsername;
    @FXML private Label lblProfileEmployeeId;
    @FXML private Label lblProfilePhone;
    @FXML private Label lblProfileLastLogin;
    @FXML private Label lblProfileCreatedAt;
    @FXML private Label lblProfileDevice;
    @FXML private Button btnSaveChanges;
    @FXML private Button btnLockUser;
    @FXML private Button btnUnlockUser;
    @FXML private Button btnDeleteUser;

    private final UserDao userDao = new UserDao();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    private User selectedUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupProfileCombos();
        filteredUsers = new FilteredList<>(userList, user -> true);
        setupTable();
        setupSearch();
        setupSelectionListener();
        setupAnalystSelector();
        loadUserData();
    }

    private void setupProfileCombos() {
        cbProfileRole.getItems().addAll("Staff", "Manager", "Supervisor", "Admin");
        cbProfileDept.getItems().addAll("Finance", "HR", "IT", "Marketing", "Design");
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredUsers.setPredicate(user -> matchesSearch(user, query));
        });
    }

    private void setupAnalystSelector() {
        AnalystSession.ensureSelected(userDao);
        if (AnalystSession.isAnalystSelected()) {
            lblAnalystName.setText("ANALYST: " + AnalystSession.getCurrentAnalystName().toUpperCase());
        }
    }

    private boolean matchesSearch(User user, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(user.getFullName(), query)
                || containsIgnoreCase(user.getUsername(), query)
                || containsIgnoreCase(user.getEmail(), query)
                || containsIgnoreCase(user.getDepartment(), query)
                || containsIgnoreCase(user.getRole(), query)
                || containsIgnoreCase(user.getEmployeeId(), query);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void setupTable() {
        nameCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getFullName()));
    }

    private void loadUserData() {
        String selectedId = selectedUser != null ? selectedUser.getEmployeeId() : null;
        userList.setAll(userDao.getAllUsers());
        System.out.println("Users loaded from DB: " + userList.size());
        userTable.setItems(filteredUsers);

        // Deferred to the next pulse: selecting a row in the same tick as
        // setItems(...) can leave the TableView still resolving the old
        // (empty) items list internally — the row LOOKS highlighted, but
        // getSelectedItem() comes back null, so the profile panel never
        // gets populated.
        Platform.runLater(() -> {
            if (selectedId != null) {
                filteredUsers.stream()
                        .filter(user -> selectedId.equals(user.getEmployeeId()))
                        .findFirst()
                        .ifPresentOrElse(
                                user -> userTable.getSelectionModel().select(user),
                                this::selectFirstOrEmpty
                        );
            } else {
                selectFirstOrEmpty();
            }
        });
    }

    private void selectFirstOrEmpty() {
        if (!filteredUsers.isEmpty()) {
            userTable.getSelectionModel().selectFirst();
        } else {
            userTable.getSelectionModel().clearSelection();
            showEmptyProfile();
        }
    }

    private void setupSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newUser) -> {
            selectedUser = newUser;
            if (newUser != null) {
                showUserDetail(newUser);
            } else {
                showEmptyProfile();
            }
        });
    }

    private void showUserDetail(User user) {
        lblProfileName.setText(user.getFullName());
        cbProfileRole.setValue(user.getRole() != null ? user.getRole() : "Staff");
        cbProfileDept.setValue(user.getDepartment() != null ? user.getDepartment() : "");
        lblAccountStatus.setText(formatAccountStatus(user));
        lblProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
        lblProfileUsername.setText(user.getUsername() != null ? user.getUsername() : "-");
        lblProfileEmployeeId.setText(user.getEmployeeId() != null ? user.getEmployeeId() : "-");
        lblProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "-");
        lblProfileDevice.setText(user.getAssignedDevice() != null ? user.getAssignedDevice() : "No data yet.");
        lblProfileLastLogin.setText(formatDateTime(user.getLastLoginAt(), "Never"));
        lblProfileCreatedAt.setText(formatDateTime(user.getCreatedAt(), "-"));
        applyAvatar(user);
        updateAccountActionButtons(user);
        setProfileActionsEnabled(true);
    }

    private String formatAccountStatus(User user) {
        boolean locked = Boolean.TRUE.equals(user.getIsLocked());
        boolean active = user.getIsActive() == null || user.getIsActive();
        if (locked) {
            return "Locked";
        }
        return active ? "Active" : "Inactive";
    }

    private String formatDateTime(LocalDateTime value, String fallback) {
        return value != null ? value.format(PROFILE_DATE_FORMAT) : fallback;
    }

    private void updateAccountActionButtons(User user) {
        boolean locked = Boolean.TRUE.equals(user.getIsLocked());
        btnLockUser.setDisable(locked);
        btnUnlockUser.setDisable(!locked);
    }

    private void setProfileActionsEnabled(boolean enabled) {
        btnSaveChanges.setDisable(!enabled);
        btnDeleteUser.setDisable(!enabled);
        cbProfileRole.setDisable(!enabled);
        cbProfileDept.setDisable(!enabled);
        if (!enabled) {
            btnLockUser.setDisable(true);
            btnUnlockUser.setDisable(true);
        }
    }

    // Shown only when the directory has zero employees
    private void showEmptyProfile() {
        selectedUser = null;
        lblProfileName.setText("Select Employee");
        cbProfileRole.setValue(null);
        cbProfileDept.setValue(null);
        lblAccountStatus.setText("-");
        lblProfileEmail.setText("-");
        lblProfileUsername.setText("-");
        lblProfileEmployeeId.setText("-");
        lblProfilePhone.setText("-");
        lblProfileDevice.setText("No data yet.");
        lblProfileLastLogin.setText("-");
        lblProfileCreatedAt.setText("-");
        applyAvatar(null);
        setProfileActionsEnabled(false);
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

    /**
     * Copies a picked photo file into the app's own storage folder instead of
     * just remembering wherever it originally lived on disk. Without this,
     * moving/renaming/deleting the original file (or opening the app on a
     * different machine) would silently break the avatar, since only the
     * raw path was ever stored.
     *
     * @param source     the file the user picked in the FileChooser
     * @param employeeId used as the stable filename so re-uploading for the
     *                   same person overwrites their old photo instead of
     *                   piling up orphaned files
     * @return the path to store in the database; falls back to the original
     *         file's path if the copy fails, so the photo still works for
     *         the current session
     */
    private String storeAvatarFile(File source, String employeeId) {
        try {
            File dir = new File(AVATAR_STORAGE_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("Could not create avatar storage folder: " + dir.getAbsolutePath());
                return source.getAbsolutePath();
            }
            String originalName = source.getName();
            int dotIndex = originalName.lastIndexOf('.');
            String extension = dotIndex >= 0 ? originalName.substring(dotIndex) : ".jpg";
            File destination = new File(dir, employeeId + extension);
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destination.getPath();
        } catch (IOException e) {
            System.err.println("Failed to copy avatar into app storage, using original path instead: " + e.getMessage());
            return source.getAbsolutePath();
        }
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

        // Laid out as a 2-column grid instead of one long vertical stack,
        // so the dialog stays a reasonable height instead of running far
        // down the screen.
        fullNameField.setPrefWidth(220);
        usernameField.setPrefWidth(220);
        emailField.setPrefWidth(220);
        phoneField.setPrefWidth(220);
        deviceField.setPrefWidth(220);
        roleBox.setPrefWidth(220);
        departmentField.setPrefWidth(220);
        passwordField.setPrefWidth(220);

        javafx.scene.layout.GridPane form = new javafx.scene.layout.GridPane();
        form.setHgap(16);
        form.setVgap(10);
        form.setPadding(new javafx.geometry.Insets(10, 5, 10, 5));

        form.add(new Label("Full Name"), 0, 0);
        form.add(fullNameField, 0, 1);
        form.add(new Label("Username"), 1, 0);
        form.add(usernameField, 1, 1);

        form.add(new Label("Email"), 0, 2);
        form.add(emailField, 0, 3);
        form.add(new Label("Phone"), 1, 2);
        form.add(phoneField, 1, 3);

        form.add(new Label("Device"), 0, 4);
        form.add(deviceField, 0, 5);
        form.add(new Label("Role"), 1, 4);
        form.add(roleBox, 1, 5);

        form.add(new Label("Department"), 0, 6);
        form.add(departmentField, 0, 7);
        form.add(new Label("Password"), 1, 6);
        form.add(passwordField, 1, 7);

        VBox photoBox = new VBox(6, choosePhotoBtn, avatarPreview);
        Label photoLabel = new Label("Profile Photo");
        form.add(photoLabel, 0, 8);
        form.add(photoBox, 0, 9);
        javafx.scene.layout.GridPane.setColumnSpan(photoLabel, 2);
        javafx.scene.layout.GridPane.setColumnSpan(photoBox, 2);

        form.getStyleClass().add("dialog-form");
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
                    String storedPath = storeAvatarFile(selectedAvatarFile[0], newUser.getEmployeeId());
                    newUser.setAvatarPath(storedPath);
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

    // ==================== Manage User ====================

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Validation", "Select an employee first.");
            return;
        }

        String role = cbProfileRole.getValue();
        String department = cbProfileDept.getValue() != null ? cbProfileDept.getValue().trim() : "";

        if (role == null || role.isBlank()) {
            showAlert("Validation", "Role is required.");
            return;
        }
        if (department.isBlank()) {
            showAlert("Validation", "Department is required.");
            return;
        }

        selectedUser.setRole(role);
        selectedUser.setDepartment(department);

        if (userDao.updateUser(selectedUser)) {
            loadUserData();
            showAlert("Success", "User updated successfully.");
        } else {
            showAlert("Failed", "Failed to save user changes.");
        }
    }

    @FXML
    private void handleLockUser(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Validation", "Select an employee first.");
            return;
        }

        // Capture the name before refreshing, so a selection change during
        // reload can never turn this into a null-pointer on the next line.
        String name = selectedUser.getFullName();
        if (userDao.lockUser(selectedUser.getEmployeeId())) {
            reloadSelectedUser();
            showAlert("Success", name + " has been locked.");
        } else {
            showAlert("Failed", "Failed to lock user account.");
        }
    }

    @FXML
    private void handleUnlockUser(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Validation", "Select an employee first.");
            return;
        }

        String name = selectedUser.getFullName();
        if (userDao.unlockUser(selectedUser.getEmployeeId())) {
            reloadSelectedUser();
            showAlert("Success", name + " has been unlocked.");
        } else {
            showAlert("Failed", "Failed to unlock user account.");
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Validation", "Select an employee first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete " + selectedUser.getFullName() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String deletedId = selectedUser.getEmployeeId();
                String avatarPath = selectedUser.getAvatarPath();
                if (userDao.deleteUser(deletedId)) {
                    deleteAvatarFileIfManaged(avatarPath);
                    selectedUser = null;
                    loadUserData();
                    showAlert("Success", "User deleted successfully.");
                } else {
                    showAlert("Failed", "Failed to delete user. The account may still be linked to tickets.");
                }
            }
        });
    }

    /**
     * Deletes the user's uploaded photo from app storage after their account
     * is removed, so orphaned files don't keep piling up in the avatars
     * folder. Only ever deletes files that actually live inside our managed
     * storage directory — never touches a path outside it, in case
     * avatarPath happens to point somewhere else.
     */
    private void deleteAvatarFileIfManaged(String avatarPath) {
        if (avatarPath == null || avatarPath.isBlank()) {
            return;
        }
        try {
            File avatarFile = new File(avatarPath).getCanonicalFile();
            File storageDir = new File(AVATAR_STORAGE_DIR).getCanonicalFile();
            if (avatarFile.getPath().startsWith(storageDir.getPath()) && avatarFile.exists()) {
                if (!avatarFile.delete()) {
                    System.err.println("Could not delete avatar file: " + avatarFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("Error while cleaning up avatar file: " + e.getMessage());
        }
    }

    private void reloadSelectedUser() {
        if (selectedUser == null) {
            return;
        }
        User refreshed = userDao.findByEmployeeId(selectedUser.getEmployeeId());
        if (refreshed != null) {
            // Update the existing selected object's fields in place instead of
            // swapping in a brand-new User instance via userList.set(...).
            // Replacing the list item with a different object identity was
            // causing the TableView's selection (wrapped by the FilteredList)
            // to lose track of "who is selected", which reset selectedUser to
            // null right after a lock/unlock/save and forced the user to
            // re-click the row before the next action would work.
            selectedUser.setIsLocked(refreshed.getIsLocked());
            selectedUser.setIsActive(refreshed.getIsActive());
            selectedUser.setRole(refreshed.getRole());
            selectedUser.setDepartment(refreshed.getDepartment());
            selectedUser.setUpdatedAt(refreshed.getUpdatedAt());

            showUserDetail(selectedUser);
            userTable.refresh();
        } else {
            loadUserData();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}