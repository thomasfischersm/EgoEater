package com.playposse.egoeater.clientactions;

import android.content.Context;

import java.io.IOException;

/**
 * A client action that reports abuse of another user to the cloud.
 */
public class ReportAbuseClientAction extends ApiClientAction<Void> {

    private final long abuserId;
    private final String note;

    public ReportAbuseClientAction(Context context, long abuserId, String note) {
        super(context);

        this.abuserId = abuserId;
        this.note = note;
    }

    @Override
    protected Void executeAsync() throws IOException {
        getApi().reportAbuse(getSessionId(), abuserId, note).execute();

        return null;
    }
}
