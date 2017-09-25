package com.ioannisgk.evsharingapp.entities;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    // Class attributes mapped to table columns in database

    private int id;
    private String username;
    private String password;
    private String name;
    private String gender;
    private Date dob;
    private String requestStatus;

    // Class constructors
    public User() {

    }

    public User(String username, String password, String name, String gender, Date dob){
        this.username = username;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
    }

    public User(String requestStatus){
        this.requestStatus = requestStatus;
    }

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    // toString method for debugging

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", name=" + name + ", gender="
                + gender + ", dob=" + dob + ", requestStatus=" + requestStatus + "]";
    }
}