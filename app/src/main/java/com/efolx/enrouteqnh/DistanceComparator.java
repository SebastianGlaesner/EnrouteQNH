package com.efolx.enrouteqnh;

import android.location.Location;

import java.util.Comparator;

/**
 * Created by Metti on 17.05.2015.
 */
public class DistanceComparator implements Comparator<Airport>{
    private Location current;

    DistanceComparator(Location current){
        this.current=current;
    }
    @Override
    public int compare(Airport lhs, Airport rhs) {
        Location leftLocation = lhs.getLocation();
        Location rightLocation = rhs.getLocation();
        return (int)(leftLocation.distanceTo(current)-rightLocation.distanceTo(current));
    }
}
