package com.itservicedesk.controller;

import com.itservicedesk.model.ActivityLog;
import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.Ticket;
import com.itservicedesk.model.User;
import com.itservicedesk.service.TicketService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TicketController implements Initializable {

    @FXML private VBox queueContainer;
    @FXML private VBox activeRoomContainer;
    @FXML private Label lblQueueCount;
    @FXML private Label lblActiveCount;
    @FXML private Label lblResolvedCount;
    @FXML private Label lblAnalystName;
    @FXML private Label lblActiveHint;
    @FXML private Label lblQueueHint;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilterPriority;
    @FXML private ComboBox<User> cbAnalyst;
    @FXML private Button btnNewTicket;
    @FXML private Button btnRefresh;
    @FXML private VBox historyContainer;
    @FXML private VBox logContainer;

    private final TicketService ticketService = new TicketService();
    private final UserDao userDao = new UserDao();
    private User currentAnalyst;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        setupAnalystSelector();
        setupCreateTicket();
        btnRefresh.setOnAction(e -> refreshAll());
        txtSearch.textProperty().addListener((obs, old, val) -> loadQueue());

        refreshAll();
    }

    private void setupFilters() {
        cbFilterPriority.getItems().addAll("All Priorities", "Critical", "High", "Medium", "Low");
        cbFilterPriority.setValue("All Priorities");
        cbFilterPriority.setOnAction(e -> loadQueue());
    }

    private void setupAnalystSelector() {
        List<User> analysts = userDao.getActiveItStaff();
        if (analysts.isEmpty()) {
            currentAnalyst = userDao.findByUsername("ahmad.fauzi");
            if (currentAnalyst != null) {
                cbAnalyst.getItems().add(currentAnalyst);
                cbAnalyst.setValue(currentAnalyst);
            }
        } else {
            cbAnalyst.getItems().addAll(analysts);
            currentAnalyst = analysts.get(0);
            cbAnalyst.setValue(currentAnalyst);
        }

        cbAnalyst.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName() + " (" + item.getDepartment() + ")");
            }
        });
        cbAnalyst.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Analyst" : item.getFullName());
            }
        });

        cbAnalyst.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentAnalyst = newVal;
            lblAnalystName.setText("ANALYST: " + (newVal != null ? newVal.getFullName().toUpperCase() : "---"));
            refreshAll();
        });

        if (currentAnalyst != null) {
            lblAnalystName.setText("ANALYST: " + currentAnalyst.getFullName().toUpperCase());
        }
    }

    private void setupCreateTicket() {
        btnNewTicket.setOnAction(e -> openCreateTicketDialog());
    }

    private void refreshAll() {
        updateTelemetry();
        loadQueue();
        loadActiveRoom();
        loadHistoryFeed();
        loadActivityLog();
    }

    private void updateTelemetry() {
        lblQueueCount.setText(String.valueOf(ticketService.getTicketDao().countByStatus("Open")));
        String analystId = currentAnalyst != null ? currentAnalyst.getEmployeeId() : null;
        lblActiveCount.setText(String.valueOf(ticketService.getTicketDao().countActiveByAssignee(analystId)));
        lblResolvedCount.setText(String.valueOf(ticketService.getTicketDao().countByStatus("Resolved")));
    }

    private void loadQueue() {
        queueContainer.getChildren().clear();
        String priorityFilter = cbFilterPriority.getValue();
        String search = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";

        List<Ticket> tickets = ticketService.getTicketDao().getQueueTickets();
        List<Ticket> filtered = tickets.stream()
                .filter(t -> matchesPriority(t, priorityFilter))
                .filter(t -> matchesSearch(t, search))
                .toList();

        if (filtered.isEmpty()) {
            lblQueueHint.setText("QUEUE CLEAR");
            Label empty = new Label("// No tickets awaiting triage.\n// Stand by for incoming incidents.");
            empty.getStyleClass().add("active-empty");
            empty.setWrapText(true);
            queueContainer.getChildren().add(empty);
            return;
        }

        lblQueueHint.setText(filtered.size() + " PENDING");
        for (Ticket ticket : filtered) {
            queueContainer.getChildren().add(createQueueCard(ticket));
        }
    }

    private void loadActiveRoom() {
        activeRoomContainer.getChildren().clear();
        String analystId = currentAnalyst != null ? currentAnalyst.getEmployeeId() : null;
        List<Ticket> activeTickets = ticketService.getTicketDao().getActiveTickets(analystId);

        if (activeTickets.isEmpty()) {
            lblActiveHint.setText("NO SESSION");
            Label empty = new Label("// Active Room idle.\n// Claim a ticket from the queue to begin resolution.");
            empty.getStyleClass().add("active-empty");
            empty.setWrapText(true);
            activeRoomContainer.getChildren().add(empty);
            return;
        }

        lblActiveHint.setText(activeTickets.size() + " SESSION" + (activeTickets.size() > 1 ? "S" : ""));
        for (Ticket ticket : activeTickets) {
            activeRoomContainer.getChildren().add(createActiveSession(ticket));
        }
    }

    private void loadHistoryFeed() {
        historyContainer.getChildren().clear();
        List<Ticket> history = ticketService.getTicketDao().getHistoryTickets().stream().limit(4).toList();
        if (history.isEmpty()) {
            Label empty = new Label("// No resolved/closed tickets yet.");
            empty.getStyleClass().add("queue-meta");
            historyContainer.getChildren().add(empty);
            return;
        }
        for (Ticket ticket : history) {
            Label item = new Label(shortId(ticket.getTicketId()) + "  " + ticket.getStatus() + "  " + truncate(ticket.getTitle(), 42));
            item.getStyleClass().add("queue-meta");
            historyContainer.getChildren().add(item);
        }
    }

    private void loadActivityLog() {
        logContainer.getChildren().clear();
        List<ActivityLog> logs = ticketService.getActivityLogDao().getRecent(6);
        if (logs.isEmpty()) {
            Label empty = new Label("// No activity log yet.");
            empty.getStyleClass().add("queue-meta");
            logContainer.getChildren().add(empty);
            return;
        }
        for (ActivityLog log : logs) {
            String ts = formatTime(log.getCreatedAt());
            Label line = new Label(ts + "  [" + log.getAction() + "]  " + log.getMessage());
            line.getStyleClass().add("queue-meta");
            line.setWrapText(true);
            logContainer.getChildren().add(line);
        }
    }

    private TitledPane createQueueCard(Ticket ticket) {
        VBox content = new VBox(10);
        content.getStyleClass().add("accordion-content");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label(shortId(ticket.getTicketId()));
        idLabel.getStyleClass().add("queue-id");

        Label badge = createPriorityBadge(ticket.getPriority());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(idLabel, badge, spacer);

        Label title = new Label(ticket.getTitle());
        title.getStyleClass().add("queue-title");
        title.setWrapText(true);

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(formatTime(ticket.getCreatedAt()));
        meta.getStyleClass().add("queue-meta");

        Button btnClaim = new Button("▶ CLAIM");
        btnClaim.getStyleClass().add("btn-claim");
        btnClaim.setOnAction(e -> claimTicket(ticket));

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        bottomRow.getChildren().addAll(meta, bottomSpacer, btnClaim);

        content.getChildren().addAll(topRow, title, bottomRow);

        String headerText = shortId(ticket.getTicketId()) + " | " + ticket.getPriority().toUpperCase() + " | " + truncate(ticket.getTitle(), 38);
        TitledPane pane = new TitledPane(headerText, content);
        pane.getStyleClass().add("accordion-pane");
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane createActiveSession(Ticket ticket) {
        VBox session = new VBox(12);
        session.getStyleClass().addAll("active-session", "accordion-content");

        VBox header = new VBox(6);
        header.getStyleClass().add("active-session-header");

        Label idLabel = new Label("TICKET " + shortId(ticket.getTicketId()));
        idLabel.getStyleClass().add("active-ticket-id");

        Label title = new Label(ticket.getTitle());
        title.getStyleClass().add("active-ticket-title");
        title.setWrapText(true);

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(
                createPriorityBadge(ticket.getPriority()),
                createStatusBadge(ticket.getStatus())
        );

        header.getChildren().addAll(idLabel, title, badges);

        VBox descBlock = new VBox(4);
        Label descLabel = new Label("INCIDENT REPORT");
        descLabel.getStyleClass().add("active-label");
        Label desc = new Label(ticket.getDescription());
        desc.getStyleClass().add("active-description");
        desc.setWrapText(true);
        descBlock.getChildren().addAll(descLabel, desc);

        HBox metaRow = new HBox(30);
        metaRow.getChildren().addAll(
                metaField("REPORTER", ticket.getReporterId()),
                metaField("CLAIMED", formatTime(ticket.getUpdatedAt())),
                metaField("CREATED", formatTime(ticket.getCreatedAt()))
        );

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnResolve = new Button("✓ RESOLVE");
        btnResolve.getStyleClass().add("btn-resolve");
        btnResolve.setOnAction(e -> resolveTicket(ticket));

        Button btnRelease = new Button("↩ RELEASE");
        btnRelease.getStyleClass().add("btn-release");
        btnRelease.setOnAction(e -> releaseTicket(ticket));

        Button btnClose = new Button("✕ CLOSE");
        btnClose.getStyleClass().add("btn-close");
        btnClose.setOnAction(e -> closeTicket(ticket));

        actions.getChildren().addAll(btnResolve, btnClose, btnRelease);

        session.getChildren().addAll(header, descBlock, metaRow, actions);

        String headerText = shortId(ticket.getTicketId()) + " | " + ticket.getStatus().toUpperCase() + " | " + truncate(ticket.getTitle(), 34);
        TitledPane pane = new TitledPane(headerText, session);
        pane.getStyleClass().add("accordion-pane");
        pane.setExpanded(true);
        return pane;
    }

    private VBox metaField(String label, String value) {
        VBox box = new VBox(2);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("active-label");
        Label val = new Label(value != null ? truncate(value, 20) : "—");
        val.getStyleClass().add("active-value");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private void claimTicket(Ticket ticket) {
        if (currentAnalyst == null) {
            showAlert("Analyst belum dipilih", "Pilih analyst dulu sebelum claim ticket.");
            return;
        }
        if (ticketService.claimTicket(ticket.getTicketId(), currentAnalyst.getEmployeeId())) {
            refreshAll();
        } else {
            showAlert("Claim gagal", "Ticket tidak bisa di-claim. Pastikan status masih Open.");
        }
    }

    private void resolveTicket(Ticket ticket) {
        if (currentAnalyst == null) {
            showAlert("Analyst belum dipilih", "Pilih analyst dulu sebelum resolve ticket.");
            return;
        }
        if (ticketService.resolveTicket(ticket.getTicketId(), currentAnalyst.getEmployeeId())) {
            refreshAll();
        } else {
            showAlert("Resolve gagal", "Ticket hanya bisa di-resolve dari status In Progress.");
        }
    }

    private void closeTicket(Ticket ticket) {
        if (currentAnalyst == null) {
            showAlert("Analyst belum dipilih", "Pilih analyst dulu sebelum close ticket.");
            return;
        }
        if (ticketService.closeTicket(ticket.getTicketId(), currentAnalyst.getEmployeeId())) {
            refreshAll();
        } else {
            showAlert("Close gagal", "Ticket hanya bisa di-close dari status In Progress/Resolved.");
        }
    }

    private void releaseTicket(Ticket ticket) {
        if (currentAnalyst == null) {
            showAlert("Analyst belum dipilih", "Pilih analyst dulu sebelum release ticket.");
            return;
        }
        if (ticketService.releaseTicket(ticket.getTicketId(), currentAnalyst.getEmployeeId())) {
            refreshAll();
        } else {
            showAlert("Release gagal", "Ticket hanya bisa di-release oleh analyst yang sedang memegang ticket tersebut.");
        }
    }

    private void openCreateTicketDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Ticket");
        dialog.setHeaderText("Add ticket to queue");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Ticket title");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Incident description");
        descArea.setPrefRowCount(4);
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Critical", "High", "Medium", "Low");
        priorityBox.setValue("Medium");
        TextField reporterField = new TextField();
        reporterField.setPromptText("Reporter ID");

        VBox form = new VBox(8,
                new Label("Title"), titleField,
                new Label("Description"), descArea,
                new Label("Priority"), priorityBox,
                new Label("Reporter ID"), reporterField
        );
        form.getStyleClass().add("dialog-form");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("workstation-dialog");

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String title = titleField.getText() != null ? titleField.getText().trim() : "";
                String desc = descArea.getText() != null ? descArea.getText().trim() : "";
                String reporter = reporterField.getText() != null ? reporterField.getText().trim() : "";
                if (title.isEmpty() || desc.isEmpty() || reporter.isEmpty()) {
                    showAlert("Validation", "Title, Description, dan Reporter ID wajib diisi.");
                    return;
                }
                Ticket ticket = new Ticket(title, desc, reporter, priorityBox.getValue());
                String analystId = currentAnalyst != null ? currentAnalyst.getEmployeeId() : "SYSTEM";
                if (ticketService.createTicket(ticket, analystId)) {
                    refreshAll();
                } else {
                    showAlert("Error", "Gagal membuat tiket baru.");
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

    private Label createPriorityBadge(String priority) {
        Label badge = new Label(priority != null ? priority.toUpperCase() : "MEDIUM");
        badge.getStyleClass().addAll("badge", getPriorityBadgeStyle(priority));
        return badge;
    }

    private Label createStatusBadge(String status) {
        String text = status != null ? status.toUpperCase() : "OPEN";
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", "badge-status-progress");
        return badge;
    }

    private String getPriorityBadgeStyle(String priority) {
        if (priority == null) return "badge-medium";
        return switch (priority.toLowerCase()) {
            case "critical" -> "badge-critical";
            case "high" -> "badge-high";
            case "medium" -> "badge-medium";
            case "low" -> "badge-low";
            default -> "badge-medium";
        };
    }

    private boolean matchesPriority(Ticket ticket, String filter) {
        if (filter == null || "All Priorities".equals(filter)) return true;
        return filter.equalsIgnoreCase(ticket.getPriority());
    }

    private boolean matchesSearch(Ticket ticket, String search) {
        if (search.isEmpty()) return true;
        return ticket.getTitle().toLowerCase().contains(search)
                || ticket.getDescription().toLowerCase().contains(search)
                || ticket.getTicketId().toLowerCase().contains(search);
    }

    private String shortId(String id) {
        if (id == null) return "???";
        return id.length() > 8 ? "#" + id.substring(0, 8).toUpperCase() : "#" + id.toUpperCase();
    }

    private String formatTime(java.time.LocalDateTime dt) {
        return dt != null ? dt.format(TIME_FMT) : "—";
    }

    private String truncate(String text, int max) {
        return text.length() > max ? text.substring(0, max) + "…" : text;
    }
}
