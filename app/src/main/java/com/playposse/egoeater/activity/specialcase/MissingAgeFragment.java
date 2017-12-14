package com.playposse.egoeater.activity.specialcase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.ParentActivity;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.UpdateBirthdayOverrideClientAction;
import com.playposse.egoeater.util.DataMunchUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A {@link Fragment} that blocks the user until the age is entered.
 */
public class MissingAgeFragment extends Fragment {

    private static final int FUTURE_AGE = 0;
    private static final int MAXIMUM_AGE = 100;

    @BindView(R.id.birthday_edit_text) EditText birthdayEditText;
    @BindView(R.id.birthday_button) Button birthdayButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView =
                inflater.inflate(R.layout.fragment_missing_age, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.birthday_button)
    public void onSubmitClicked() {
        // Attempt to parse the age.
        String birthdayStr = birthdayEditText.getText().toString();
        Integer age = DataMunchUtil.getAge(birthdayStr);

        // Check for parsable age.
        if (age == null) {
            birthdayEditText.setError(getString(R.string.age_request_required_error));
            return;
        }

        // Check for plausible age range.
        if (age <= FUTURE_AGE) {
            birthdayEditText.setError(getString(R.string.age_request_future_error));
            return;
        } else if (age >= MAXIMUM_AGE) {
            birthdayEditText.setError(getString(R.string.age_request_super_old_error));
            return;
        }

        // Save age override.
        ParentActivity activity = (ParentActivity) getActivity();
        if (activity != null) {
            activity.showLoadingProgress();
        }
        new UpdateBirthdayOverrideClientAction(
                getActivity(),
                birthdayStr,
                new ApiClientAction.Callback<UserBean>() {
                    @Override
                    public void onResult(UserBean data) {
                        onCloudComplete();
                    }
                })
                .execute();
    }

    private void onCloudComplete() {
        ParentActivity activity = (ParentActivity) getActivity();
        if (activity != null) {
            activity.showLoadingProgress();

            GlobalRouting.onStartComparing(activity);
        }
    }
}
