package com.itservicedesk.controller;

import com.itservicedesk.dao.TicketDao;
import com.itservicedesk.model.Ticket;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TicketController implements Initializable {

    @FXML
    private VBox ticketContainer;

    @FXML
    private ComboBox<String> cbFilterPriority;

    @FXML
    private ComboBox<String> cbSortBy;

    @FXML
    private Button btnSeedTicket;

    private final TicketDao ticketDao = new TicketDao();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("=== TicketController initialize() dipanggil ===");

        setupFilters();

        if (ticketContainer == null) {
            System.err.println("ERROR: ticketContainer is NULL! Cek fx:id di FXML");
        } else {
            System.out.println("ticketContainer ditemukan, mulai loadTickets...");
            loadTickets();
        }

        // Seed button
        if (btnSeedTicket != null) {
            btnSeedTicket.setOnAction(e -> {
                System.out.println("Seed button diklik");
                loadTickets();
            });
        }
    }

    public void loadTickets() {
        System.out.println("loadTickets() dipanggil");
        ticketContainer.getChildren().clear();

        List<Ticket> tickets = ticketDao.getAllTickets();
        System.out.println("Jumlah tiket dari DAO: " + tickets.size());

        if (tickets.isEmpty()) {
            System.out.println("Tidak ada tiket, tampilkan pesan kosong");
            Label emptyLabel = new Label("Belum ada tiket. Klik 'Seed Dummy Ticket' untuk mengisi data.");
            emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 16px;");
            ticketContainer.getChildren().add(emptyLabel);
            return;
        }

        System.out.println("Membuat " + tickets.size() + " ticket card...");
        for (Ticket ticket : tickets) {
            try {
                VBox card = createTicketCard(ticket);
                ticketContainer.getChildren().add(card);
                System.out.println("Card dibuat untuk ticket: " + ticket.getTicketId());
            } catch (Exception ex) {
                System.err.println("Error membuat card untuk ticket " + ticket.getTicketId() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void setupFilters() {
        if (cbFilterPriority != null) {
            cbFilterPriority.getItems().addAll("Semua Prioritas", "Critical", "High", "Medium", "Low");
            cbFilterPriority.setValue("Semua Prioritas");
            cbFilterPriority.setOnAction(e -> loadTickets());
        }

        if (cbSortBy != null) {
            cbSortBy.getItems().addAll("Terbaru", "Prioritas Tertinggi", "Status");
            cbSortBy.setValue("Terbaru");
            cbSortBy.setOnAction(e -> loadTickets());
        }
    }

    private VBox createTicketCard(Ticket ticket) {
        VBox card = new VBox(10);
        card.getStyleClass().add("ticket-card");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(ticket.getTitle());
        title.getStyleClass().add("ticket-title");

        Label status = new Label("[" + ticket.getStatus() + "]");
        status.getStyleClass().add(getStatusStyle(ticket.getStatus()));

        Label priority = new Label(ticket.getPriority());
        priority.getStyleClass().add(getPriorityStyle(ticket.getPriority()));

        Button btnDetail = new Button("Detail ▼");
        btnDetail.getStyleClass().add("action-button");

        header.getChildren().addAll(title, status, priority, btnDetail);

        // Detail Panel (awalnya tersembunyi)
        VBox detailPanel = new VBox(8);
        detailPanel.getStyleClass().add("ticket-detail");
        detailPanel.setVisible(false);

        Label desc = new Label("Deskripsi: " + ticket.getDescription());
        desc.setWrapText(true);

        HBox actions = new HBox(10);
        Button btnClaim = new Button("Claim");
        Button btnResolve = new Button("Resolve");
        Button btnClose = new Button("Close");

        btnClaim.getStyleClass().addAll("action-button", "btn-claim");
        btnResolve.getStyleClass().addAll("action-button", "btn-resolve");
        btnClose.getStyleClass().addAll("action-button", "btn-close");

        actions.getChildren().addAll(btnClaim, btnResolve, btnClose);

        detailPanel.getChildren().addAll(desc, actions);

        // Toggle Detail
        btnDetail.setOnAction(e -> {
            boolean isVisible = !detailPanel.isVisible();
            detailPanel.setVisible(isVisible);
            btnDetail.setText(isVisible ? "Detail ▲" : "Detail ▼");
        });

        card.getChildren().addAll(header, detailPanel);
        return card;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "status-open";
        return switch (status.toLowerCase()) {
            case "open" -> "status-open";
            case "in progress" -> "status-in-progress";
            case "resolved" -> "status-resolved";
            case "closed" -> "status-closed";
            default -> "status-open";
        };
    }

    private String getPriorityStyle(String priority) {
        if (priority == null) return "priority-medium";
        return switch (priority.toLowerCase()) {
            case "critical" -> "priority-critical";
            case "high" -> "priority-high";
            case "medium" -> "priority-medium";
            case "low" -> "priority-low";
            default -> "priority-medium";
        };
    }
}