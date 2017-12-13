package com.playposse.egoeater.clientactions.admin;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.AdminEgoEaterUserBean;
import com.playposse.egoeater.backend.egoEaterApi.model.AdminEgoEaterUserBeanCollection;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;
import java.util.List;

/**
 * A client action that retrieves all the EgoEaterUser data from the cloud for an admin.
 */
public class GetAdminDumpEgoEaterUserClientAction
        extends ApiClientAction<List<AdminEgoEaterUserBean>> {


    public GetAdminDumpEgoEaterUserClientAction(
            Context context,
            Callback<List<AdminEgoEaterUserBean>> callback) {

        super(context, callback);
    }

    @Override
    protected List<AdminEgoEaterUserBean> executeAsync() throws IOException {
        if (!EgoEaterPreferences.isAdmin(getContext())) {
            throw new IllegalStateException(
                    "This user is not and admin and is trying to make an admin cloud call!");
        }

        AdminEgoEaterUserBeanCollection result =
                getApi().getAdminEgoEateruserDump(getSessionId()).execute();
        return result.getItems();
    }
}
