package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.ProfileIdList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A client action that gets all the profiles within a certain distance.
 */
public class GetProfileIdsByDistanceClientAction extends ApiClientAction<List<Long>> {

    private final int queryRadius;

    public GetProfileIdsByDistanceClientAction(
            Context context,
            Callback callback,
            int queryRadius) {

        super(context, callback);

        this.queryRadius = queryRadius;
    }

    @Override
    protected List<Long> executeAsync() throws IOException {
        List<Long> profileIds =
                getApi()
                        .getProfileIdsByDistance(getSessionId(), (double) queryRadius)
                        .execute()
                        .getProfileIds();

        if (profileIds == null) {
            profileIds = new ArrayList<>(0);
        }
        return profileIds;
    }

    public static List<Long> getBlocking(Context context, int queryRadius)
            throws InterruptedException {

        GetProfileIdsByDistanceClientAction action =
                new GetProfileIdsByDistanceClientAction(context, null, queryRadius);
        return action.executeBlocking();
    }
}
