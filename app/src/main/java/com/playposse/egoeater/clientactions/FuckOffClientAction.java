package com.playposse.egoeater.clientactions;

import android.content.Context;

import java.io.IOException;

/**
 * A client action that tells another user to fuck off.
 */
public class FuckOffClientAction extends ApiClientAction<Void> {

    private final long partnerId;

    public FuckOffClientAction(Context context, long partnerId) {
        super(context);

        this.partnerId = partnerId;
    }

    @Override
    protected Void executeAsync() throws IOException {
        getApi().fuckOff(getSessionId(), partnerId).execute();
        return null;
    }
}
