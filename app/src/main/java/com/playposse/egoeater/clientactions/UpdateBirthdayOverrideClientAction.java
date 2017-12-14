package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;

import java.io.IOException;

/**
 * A client action that overrides the birthday of the user.
 */
public class UpdateBirthdayOverrideClientAction extends ApiClientAction<UserBean> {

    private final String birthdayOverride;

    public UpdateBirthdayOverrideClientAction(
            Context context,
            String birthdayOverride,
            Callback<UserBean> callback) {

        super(context, callback);

        this.birthdayOverride = birthdayOverride;
    }

    @Override
    protected UserBean executeAsync() throws IOException {
        UserBean userBean = getApi().updateBirthdayOverride(getSessionId(), birthdayOverride).execute();

        AnalyticsUtil.reportUpdateBirthdayOverride(getContext());

        EgoEaterPreferences.setUser(getContext(), userBean);

        return userBean;
    }
}
