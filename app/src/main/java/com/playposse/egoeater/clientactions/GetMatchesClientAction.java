package com.playposse.egoeater.clientactions;


import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.MatchBean;
import com.playposse.egoeater.backend.egoEaterApi.model.MatchBeanCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A client action that requests the user's matches from the cloud.
 */
public class GetMatchesClientAction extends ApiClientAction<List<MatchBean>> {

    public GetMatchesClientAction(Context context, Callback<List<MatchBean>> callback) {
        super(context, callback);
    }

    @Override
    protected List<MatchBean> executeAsync() throws IOException {
        MatchBeanCollection matchBeanCollection = getApi().getMatches(getSessionId()).execute();

        if ((matchBeanCollection != null) && (matchBeanCollection.getItems() != null)) {
            return matchBeanCollection.getItems();
        } else {
            return new ArrayList<>();
        }
    }

    public static List<MatchBean> getBlocking(Context context) throws InterruptedException {
        return new GetMatchesClientAction(context, null).executeBlocking();
    }
}
