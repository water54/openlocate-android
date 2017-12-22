package com.openlocate.android.core;

import java.util.ArrayList;
import java.util.List;

final class LocationList implements LocationDataSource {

    List<OpenLocateLocation> locations;

    LocationList() {
        this.locations = new ArrayList<>();
    }

    @Override
    public void addAll(List<OpenLocateLocation> locationList) {
        this.locations.addAll(locationList);
    }

    @Override
    public void add(OpenLocateLocation location) {
        this.locations.add(location);
    }

    @Override
    public List<OpenLocateLocation> getSince(long millisecondsSince1970) {
        List<OpenLocateLocation> locations = new ArrayList<OpenLocateLocation>();
        for (OpenLocateLocation location : this.locations) {
            if (location.getCreated().getTime() > millisecondsSince1970) {
                locations.add(location);
            }
        }
        return locations;
    }

    @Override
    public void deleteBefore(long millisecondsSince1970) {
        List<OpenLocateLocation> locations = new ArrayList<OpenLocateLocation>();
        for (OpenLocateLocation location : this.locations) {
            if (location.getCreated().getTime() <= millisecondsSince1970) {
                locations.add(location);
            }
        }
        this.locations.removeAll(locations);
    }

    @Override
    public long size() {
        return this.locations.size();
    }

    @Override
    public void close() {

    }
}
