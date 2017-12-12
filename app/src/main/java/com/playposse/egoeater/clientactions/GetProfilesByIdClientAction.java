package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A client action that loads the data of profiles.
 */
public class GetProfilesByIdClientAction extends ApiClientAction<List<ProfileBean>> {

    private final List<Long> profileIds;

    public GetProfilesByIdClientAction(
            Context context,
            Callback<List<ProfileBean>> callback,
            List<Long> profileIds) {

        super(context, callback);

        this.profileIds = profileIds;
    }

    @Override
    protected List<ProfileBean> executeAsync() throws IOException {
        if ((profileIds == null) || (profileIds.size() == 0)) {
            return new ArrayList<>(0);
        } else {
            List<ProfileBean> profileBeans =
                    getApi().getProfilesById(profileIds, getSessionId()).execute().getItems();
            if (profileBeans == null) {
                profileBeans = new ArrayList<>();
            }
            return profileBeans;
        }
    }

    public static List<ProfileBean> getBlocking(Context context, List<Long> profileIds)
            throws InterruptedException {

        return new GetProfilesByIdClientAction(context, null, profileIds).executeBlocking();
    }
}
