package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

import javax.annotation.Nullable;

/**
 * A client action that deactivates and reactivates the user's account.
 */
public class UpdateAccountStatusClientAction extends ApiClientAction<UserBean> {

    private final boolean newActiveState;
    @Nullable private final String reason;

    public UpdateAccountStatusClientAction(
            Context context,
            Callback<UserBean> callback,
            boolean newActiveState,
            @Nullable String reason) {

        super(context, callback);

        this.newActiveState = newActiveState;
        this.reason = reason;
    }

    @Override
    protected UserBean executeAsync() throws IOException {
        final UserBean userBean;
        if (!newActiveState) {
            userBean = getApi()
                    .deactivateAccount(getSessionId())
                    .setReason(reason)
                    .execute();
        } else {
            userBean = getApi()
                    .reactivateAccount(getSessionId())
                    .execute();
        }

        EgoEaterPreferences.setUser(getContext(), userBean);

        return userBean;
    }
}
