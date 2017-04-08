package com.playposse.egoeater.scripts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.playposse.egoeater.util.ApiTestUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * A script that will initialize the test users.
 */
@RunWith(AndroidJUnit4.class)
public class InitTestUsersScript {

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Test
    public void run() throws IOException, ExecutionException, InterruptedException {
        Context context = InstrumentationRegistry.getContext();
        ApiTestUtil.initFakeUsers(context);
    }
}
