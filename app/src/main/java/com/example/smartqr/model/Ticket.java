package com.example.smartqr.model;

import java.io.Serializable;

public class Ticket implements Serializable {
    private String id;
    private String name;
    private String mobile;
    private String email;
    private String description;
    private String paymentStatus;
    private String status;
    private String date;
    private double amount;
    private String ticketId;

    public Ticket(String id, String name, String mobile, String description,
                  String paymentStatus, String status, String date, double amount) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.description = description;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.date = date;
        this.amount = amount;
        this.ticketId = "TKT-" + id;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
}