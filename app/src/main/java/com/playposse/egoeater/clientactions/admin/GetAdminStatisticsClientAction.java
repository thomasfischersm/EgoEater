package com.playposse.egoeater.clientactions.admin;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.AdminStatisticsBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that retrieves statistics about the users for an admin.
 */
public class GetAdminStatisticsClientAction extends ApiClientAction<AdminStatisticsBean> {


    public GetAdminStatisticsClientAction(Context context, Callback<AdminStatisticsBean> callback) {
        super(context, callback);
    }

    @Override
    protected AdminStatisticsBean executeAsync() throws IOException {
        if (!EgoEaterPreferences.isAdmin(getContext())) {
            throw new IllegalStateException(
                    "This user is not and admin and is trying to make an admin cloud call!");
        }

        return getApi().getAdminStatistics(getSessionId()).execute();
    }
}
