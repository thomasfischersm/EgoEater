package com.playposse.egoeater.clientactions.admin;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.AdminMessageBean;
import com.playposse.egoeater.backend.egoEaterApi.model.AdminMessageBeanCollection;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;
import java.util.List;

/**
 * A client action that retrieves all the message data from the cloud for an admin.
 */
public class GetAdminDumpMessageClientAction
        extends ApiClientAction<List<AdminMessageBean>> {


    public GetAdminDumpMessageClientAction(
            Context context,
            Callback<List<AdminMessageBean>> callback) {

        super(context, callback);
    }

    @Override
    protected List<AdminMessageBean> executeAsync() throws IOException {
        if (!EgoEaterPreferences.isAdmin(getContext())) {
            throw new IllegalStateException(
                    "This user is not and admin and is trying to make an admin cloud call!");
        }

        AdminMessageBeanCollection result =
                getApi().getAdminMessageDump(getSessionId()).execute();
        return result.getItems();
    }
}
