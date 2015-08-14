package com.geekoders.smartbin.graphUtilities;

/**
 * Created by kovbh01 on 8/6/2015.
 */
public class Vertex {

    final private double latitude;
    final private double longitude;


    public Vertex(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
