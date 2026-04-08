package com.example.myapplication;

public class Theater {
    private String id;
    private String name;
    private String location;

    public Theater() {}

    public Theater(String id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
}
