package com.example.myapplication;

import java.util.Date;

public class Ticket {
    private String id;
    private String userId;
    private String showtimeId;
    private String seatNumber;
    private Date purchaseDate;

    public Ticket() {}

    public Ticket(String id, String userId, String showtimeId, String seatNumber, Date purchaseDate) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.purchaseDate = purchaseDate;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getShowtimeId() { return showtimeId; }
    public String getSeatNumber() { return seatNumber; }
    public Date getPurchaseDate() { return purchaseDate; }
}
