package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that sends the location info to the cloud.
 */
public class UpdateLocationClientAction extends ApiClientAction<Void> {

    private final double latitude;
    private final double longitude;
    private final String city;
    private final String state;
    private final String country;

    public UpdateLocationClientAction(
            Context context,
            double latitude,
            double longitude,
            String city,
            String state,
            String country) {

        super(context);

        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    @Override
    protected Void executeAsync() throws IOException {
        // Send location information to the cloud.
        UserBean userBean = getApi()
                .updateLocation(getSessionId(), latitude, longitude, city, country)
                .setState(state) // Some countries don't have a state.
                .execute();

        // Store the information on the device.
        EgoEaterPreferences.setUser(getContext(), userBean);

        return null;
    }
}
