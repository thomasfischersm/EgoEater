package com.playposse.egoeater.clientactions;

import android.content.Context;

import java.io.IOException;

/**
 * A client action that tells the cloud that the user has seen a particular message.
 */
public class ReportMessageReadClientAction extends ApiClientAction<Void> {

    private final long senderProfileId;
    private final int messageIndex;

    public ReportMessageReadClientAction(Context context, long senderProfileId, int messageIndex) {
        super(context);

        this.senderProfileId = senderProfileId;
        this.messageIndex = messageIndex;
    }

    @Override
    protected Void executeAsync() throws IOException {
        getApi().reportMessageRead(getSessionId(), senderProfileId, messageIndex);
        return null;
    }
}
