package com.playposse.egoeater.backend;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.egoeater.util.ApiTestUtil;
import com.playposse.egoeater.util.FakeUser;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * A JUnit test that generates data for a male test user.
 */
@RunWith(AndroidJUnit4.class)
public class GenerateMatchesServletTest_GenerateMaleData extends AbstractApiTest {

    @Override
    protected int getFakeUserIndex() {
        return 2;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Test
    public void generateMaleData() throws InterruptedException, ExecutionException, IOException {
        // Init fake users.
        Context context = InstrumentationRegistry.getContext();
        ApiTestUtil.initFakeUsers(context);

        // Pick users.
        FakeUser april = ApiTestUtil.fakeUsers[0];
        FakeUser cindy = ApiTestUtil.fakeUsers[1];
        FakeUser james = ApiTestUtil.fakeUsers[2];
        FakeUser henry = ApiTestUtil.fakeUsers[3];

        // Report ranking by male.
        getApi()
                .reportRanking(getSessionId(), april.getProfileId(), cindy.getProfileId())
                .execute();

        // Report ranking by female.
        getApi()
                .reportRanking(april.getSessionId(), james.getProfileId(), henry.getProfileId())
                .execute();
    }
}
