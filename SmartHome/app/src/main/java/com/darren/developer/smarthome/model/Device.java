package com.darren.developer.smarthome.model;

/**
 * Created by user on 31-03-2018.
 */

public class Device {
    private String dname;
    private boolean status;
    private int port;
    private float rating;

    public Device(){
    }

    public Device(String dname, boolean status, int port, float rating) {
        this.dname = dname;
        this.status = status;
        this.port = port;
        this.rating = rating;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
