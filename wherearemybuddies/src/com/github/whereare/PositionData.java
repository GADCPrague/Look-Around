package com.github.whereare;

import android.location.Location;

/**
 * Collects data about position bearing from our current position and distance.
 * @author radim
 */
public class PositionData {
    private final Location referencePoint;
    private final Location location;
    private final float bearing;
    /** Distance (in meters). */
    private final float distance;

    public PositionData(Location referencePoint, Location location) {
        this.referencePoint = referencePoint;
        this.location = location;
        bearing = referencePoint.bearingTo(location);
        distance = referencePoint.distanceTo(location);
    }

    public float getBearing() {
        return bearing;
    }

    public float getDistance() {
        return distance;
    }

    public Location getLocation() {
        return location;
    }

    public Location getReferencePoint() {
        return referencePoint;
    }
}
