package com.playposse.egoeater.util.geocoder.retrofit;

import java.util.List;

/**
 * The root class for the retrofit request of geo coding from Google Maps.
 */
public class GeoResultRoot {

    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }
}
