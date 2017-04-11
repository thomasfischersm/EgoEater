package com.playposse.egoeater.clientactions;

import android.content.Context;

import java.io.IOException;

/**
 * A client action that reports a ranking choice by the user.
 */
public class ReportRankingClientAction extends ApiClientAction<Void> {

    private final long winnerId;
    private final long loserId;

    public ReportRankingClientAction(Context context, long winnerId, long loserId) {
        super(context);

        this.winnerId = winnerId;
        this.loserId = loserId;
    }

    @Override
    protected Void executeAsync() throws IOException {
        getApi().reportRanking(getSessionId(), winnerId, loserId).execute();
        return null;
    }
}
