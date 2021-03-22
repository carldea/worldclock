package com.carlfx.worldclock;

public class RowLocation {
    private int index;
    private Location location;

    public RowLocation(int index, Location location) {
        this.index = index;
        this.location = location;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
