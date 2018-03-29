package com.example.hal9000.trafficlightapp;

/**
 * Created by HAL 9000 on 29/03/2018.
 */

public class trafficLight
{
    private int id;
    public trafficLight(int id)
    {
        this.id = id;
    }

    public void printDetails()
    {
        System.out.println(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
