package com.playposse.egoeater.util.geocoder.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * A Retrofit service to access Google Maps geocoding.
 */
public interface GoogleMapsGeoService {

    @GET("maps/api/geocode/json?sensor=true")
    Call<GeoResultRoot> get(
            @Query("latlng") String latLng,
            @Query("language") String country,
            @Query("key") String key);
}
