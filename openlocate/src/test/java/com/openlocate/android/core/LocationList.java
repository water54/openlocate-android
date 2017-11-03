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
    public List<OpenLocateLocation> popAll() {
        List<OpenLocateLocation> locations = this.locations;
        this.locations.clear();
        return locations;
    }

    @Override
    public long size() {
        return this.locations.size();
    }

    @Override
    public void close() {

    }
}
