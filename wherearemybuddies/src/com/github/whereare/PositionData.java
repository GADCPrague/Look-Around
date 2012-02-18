package com.github.whereare;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Collects data about position bearing from our current position and distance.
 * @author radim
 */
public class PositionData implements Parcelable {
    private final Location referencePoint;
    private final Location location;
    private final float bearing;
    /** Distance (in meters). */
    private final float distance;
    private final String name;
    
    public static final Parcelable.Creator<PositionData> CREATOR = new Parcelable.Creator<PositionData>() {

        public PositionData createFromParcel(Parcel source) {
            return new PositionData(
                    (Location) source.readParcelable(null), 
                    (Location) source.readParcelable(null), 
                    source.readString());
        }

        public PositionData[] newArray(int size) {
            return new PositionData[size];
        }
    };

    public PositionData(Location referencePoint, Location location, String name) {
        this.referencePoint = referencePoint;
        this.location = location;
        bearing = referencePoint.bearingTo(location);
        distance = referencePoint.distanceTo(location);
        this.name = name;
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

    public String getName() {
        return name;
    }

    public int describeContents() {
        return 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(referencePoint, flags);
        dest.writeParcelable(location, flags);
        dest.writeString(name);
    }
}
