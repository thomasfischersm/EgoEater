package com.playposse.egoeater.clientactions;

import android.content.Context;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.MaxMessageIndexResponseBean;

import java.io.IOException;

/**
 * A client action that retrieves the maximum message index for a conversation. This is used by the
 * device to check if there are a fresh messages in the cloud.
 */
public class GetMaxMessageIndexClientAction extends ApiClientAction<Integer> {

    private static final String LOG_TAG = GetMaxMessageIndexClientAction.class.getSimpleName();

    private final long partnerId;

    public GetMaxMessageIndexClientAction(Context context, long partnerId) {
        super(context);

        this.partnerId = partnerId;
    }

    @Override
    protected Integer executeAsync() throws IOException {
        MaxMessageIndexResponseBean responseBean =
                getApi().getMaxMessageIndex(getSessionId(), partnerId).execute();
        return responseBean.getMaxMessageIndex();
    }
}
