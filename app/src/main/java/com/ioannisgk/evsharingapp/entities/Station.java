package com.ioannisgk.evsharingapp.entities;

public class Station {

    // Class attributes

    private int id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer trafficLevel;

    // Class constructor
    public Station() {

    }

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getTrafficLevel() {
        return trafficLevel;
    }

    public void setTrafficLevel(Integer trafficLevel) {
        this.trafficLevel = trafficLevel;
    }

    // toString method for debugging

    @Override
    public String toString() {
        return "Station [id=" + id + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude
                + ", trafficLevel=" + trafficLevel + "]";
    }
}