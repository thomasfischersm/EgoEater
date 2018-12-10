package com.playposse.egoeater.backend;

import androidx.test.runner.AndroidJUnit4;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBeanCollection;
import com.playposse.egoeater.backend.egoEaterApi.model.ProfileIdList;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * A test that makes an API call to {@code GetProfilesByDistanceServerAction}.
 */
@RunWith(AndroidJUnit4.class)
public class GetProfilesByDistanceServerActionApiTest extends AbstractApiTest {

    private static final String LOG_TAG =
            GetProfilesByDistanceServerActionApiTest.class.getSimpleName();

    @Test
    public void getProfilesByDistance() throws IOException {
        ProfileIdList profileIds = getApi().getProfileIdsByDistance(getSessionId(), 10.0).execute();
        Log.i(LOG_TAG, "getProfilesByDistance: Got profile ids for distance 10 miles: "
                + profileIds.getProfileIds().size());

        ProfileBeanCollection profiles =
                getApi().getProfilesById(profileIds.getProfileIds(), getSessionId()).execute();
        Log.i(LOG_TAG, "getProfilesByDistance: Got profiles for distance 10 miles: "
                + profiles.getItems().size());

        for (ProfileBean profileBean : profiles.getItems()) {
            Log.i(LOG_TAG, "getProfilesByDistance: Got profile for " + profileBean.getFirstName()
                    + " with gender " + profileBean.getGender());
        }
    }
}
