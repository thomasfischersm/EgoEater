package com.playposse.egoeater.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.UpdateAccountStatusClientAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An {@link Activity} that lets the user close the account.
 */
public class DeactivateAccountActivity extends ParentActivity {

    @BindView(R.id.pre_deactivation_layout) LinearLayout preDeactivationLayout;
    @BindView(R.id.deactivation_reason_edit_text) EditText deactivationReasonEditText;
    @BindView(R.id.confirmation_button) Button confirmationButton;
    @BindView(R.id.cancel_button) Button cancelButton;
    @BindView(R.id.confirmation_text_view) TextView confirmationTextView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_deactivate_account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.cancel_button)
    public void onCancelClicked() {
        GlobalRouting.onLoginComplete(this);
    }

    @OnClick(R.id.confirmation_button)
    public void onConfirmationClicked() {
        // Deactivate the account.
        String reason = deactivationReasonEditText.getText().toString().trim();

        new UpdateAccountStatusClientAction(
                this,
                new ApiClientAction.Callback<UserBean>() {
                    @Override
                    public void onResult(UserBean data) {
                        onCloudCompleted();
                    }
                },
                false,
                reason)
                .execute();

        showLoadingProgress();
    }

    private void onCloudCompleted() {
        dismissLoadingProgress();

        preDeactivationLayout.setVisibility(View.GONE);
        confirmationTextView.setVisibility(View.VISIBLE);
    }
}
