package com.example.hal9000.trafficlightapp;

/**
 * Created by HAL 9000 on 29/03/2018.
 */

public class trafficLight
{
    private int id;
    private String state;
    private String substate;
    private String typology;
    private String mode;
    private String density;
    private int distance;
    private String country;

    public trafficLight(int id, String state, String substate, String typology, String mode, String density, int distance, String country, int battery) {
        this.id = id;
        this.state = state;
        this.substate = substate;
        this.typology = typology;
        this.mode = mode;
        this.density = density;
        this.distance = distance;
        this.country = country;
        this.battery = battery;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSubstate() {
        return substate;
    }

    public void setSubstate(String substate) {
        this.substate = substate;
    }

    public String getTypology() {
        return typology;
    }

    public void setTypology(String typology) {
        this.typology = typology;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    private int battery;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
