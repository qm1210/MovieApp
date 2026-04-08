package com.example.myapplication;

import java.util.Date;

public class Showtime {
    private String id;
    private String movieId;
    private String theaterId;
    private Date time;
    private double price;

    public Showtime() {}

    public Showtime(String id, String movieId, String theaterId, Date time, double price) {
        this.id = id;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.time = time;
        this.price = price;
    }

    public String getId() { return id; }
    public String getMovieId() { return movieId; }
    public String getTheaterId() { return theaterId; }
    public Date getTime() { return time; }
    public double getPrice() { return price; }
}
