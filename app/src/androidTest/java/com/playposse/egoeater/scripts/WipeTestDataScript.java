package com.playposse.egoeater.scripts;

import androidx.test.runner.AndroidJUnit4;

import com.playposse.egoeater.backend.egoEaterApi.EgoEaterApi;
import com.playposse.egoeater.util.ApiTestUtil;
import com.playposse.egoeater.util.FacebookSecrets;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * A script to wipe test data.
 */
@RunWith(AndroidJUnit4.class)
public class WipeTestDataScript {

    @Test
    public void wipeTestData() throws IOException {
        EgoEaterApi api = ApiTestUtil.instantiateApi();
        api.wipeTestData(FacebookSecrets.API_SECRET).execute();
    }
}
