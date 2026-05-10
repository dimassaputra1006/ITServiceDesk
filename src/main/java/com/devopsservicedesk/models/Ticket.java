package com.devopsservicedesk.models;

public class Ticket {
    private int idTicket;
    private String judulTicket;
    private String descProblem;
    private String statusTicket;
    private int idReporter;
    private int idTechnician;

    public Ticket() {
    }

    public Ticket(int idTicket, String judulTicket, String descProblem, String statusTicket, int idReporter, int idTechnician) {
        this.idTicket = idTicket;
        this.judulTicket = judulTicket;
        this.descProblem = descProblem;
        this.statusTicket = statusTicket;
        this.idReporter = idReporter;
        this.idTechnician = idTechnician;
    }

    public int getIdTicket() {
        return idTicket;
    }

    // setter
    public void setIdTicket(int idTicket) {
        this.idTicket = idTicket;
    }

    public String getJudulTicket() {
        return judulTicket;
    }

    public void setJudulTicket(String judulTicket) {
        this.judulTicket = judulTicket;
    }

    public String getDescProbelm() {
        return descProblem;
    }

    public void setDescProblem(String descProblem) {
        this.descProblem = descProblem;
    }

    public String getStatusTicket() {
        return statusTicket;
    }

    public void setStatusTicket(String statusTicket) {
        this.statusTicket = statusTicket;
    }

    public int getIdReporter() {
        return idReporter;
    }

    public void setIdReporter(int idReporter) {
        this.idReporter = idReporter;
    }

    public int getIdTechnician(){
        return idTechnician;
    }

    public void setIdTechnician(int idTechnician) {
        this.idTechnician = idTechnician;
    }
}