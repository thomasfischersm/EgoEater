package com.playposse.egoeater.activity;

import android.app.Activity;
import android.os.Bundle;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.base.ParentActivity;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.UpdateAccountStatusClientAction;
import com.playposse.egoeater.util.AnalyticsUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An {@link Activity} that lets the user reactivate the account.
 */
public class ReactivateAccountActivity extends ParentActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_reactivate_account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.reactivate_account_button)
    public void onActivateAccountClicked() {
        // Reactivate the account.
        new UpdateAccountStatusClientAction(
                this,
                new ApiClientAction.Callback<UserBean>() {
                    @Override
                    public void onResult(UserBean data) {
                        onCloudCompleted();
                    }
                },
                true,
                null)
                .execute();

        showLoadingProgress();
    }

    private void onCloudCompleted() {
        dismissLoadingProgress();

        GlobalRouting.onStartup(this);

        AnalyticsUtil.reportReactivateAccount(getApplication());
    }
}
