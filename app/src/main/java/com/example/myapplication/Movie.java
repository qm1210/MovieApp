package com.example.myapplication;

public class Movie {
    private String id;
    private String title;
    private String genre;
    private String description;
    private String imageUrl;

    public Movie() {}

    public Movie(String id, String title, String genre, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}
