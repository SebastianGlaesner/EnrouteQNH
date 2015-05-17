package com.efolx.enrouteqnh;

import android.location.Location;

/**
 * Created by Sebastian on 16.05.2015.
 */
public class Airport implements Comparable{
    private String id;
    private String name;
    private Location location;
    private int elevation;

    public Airport(String id, String name, Location location, int elevation) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.elevation = elevation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    @Override
    public int compareTo(Object another) {
        Airport other = (Airport) another;
        return this.id.compareTo(other.id);
    }
}
