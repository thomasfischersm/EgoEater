package com.playposse.egoeater.activity.specialcase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.RatingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A {@link Fragment} that blocks the user from getting to the {@link RatingActivity} until the
 * user's location is identified.
 */
public class NoLocationFragment extends Fragment {

    private static final String LOG_TAG = NoLocationFragment.class.getSimpleName();

    @BindView(R.id.permission_button) Button permissionButton;
    @BindView(R.id.location_button) Button locationButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView =
                inflater.inflate(R.layout.fragment_no_location, container, false);

        ButterKnife.bind(this, rootView);

        refreshView();

        getNoLocationActivity().requestLocation(true);

        return rootView;
    }

    void refreshView() {
        Log.i(LOG_TAG, "refreshView: starting NoLocationFragment.refreshView");

        boolean hasPermission = getNoLocationActivity().hasLocationPermission();
        permissionButton.setVisibility(hasPermission ? View.GONE : View.VISIBLE);

        boolean hasFullLocation = getNoLocationActivity().hasFullLocation();
        locationButton.setVisibility(hasPermission && !hasFullLocation ? View.VISIBLE : View.GONE);
//
//        if (hasPermission && hasFullLocation) {
//            // Ready to move on to RatingFragment.
//            GlobalRouting.onStartComparing(getActivity());
//        } else if (hasPermission) {
//            getNoLocationActivity().requestLocation();
//        }
    }

    @OnClick(R.id.permission_button)
    public void onPermissionButtonClicked() {
        Log.i(LOG_TAG, "onPermissionButtonClicked: permission button has been clicked.");
        getNoLocationActivity().requestPermission();
    }

    @OnClick(R.id.location_button)
    public void onLocationButtonClicked() {
        Log.i(LOG_TAG, "onLocationButtonClicked: get location button has been clicked.");
        getNoLocationActivity().requestLocation(true);
    }

    private NoLocationActivity getNoLocationActivity() {
        return (NoLocationActivity) getActivity();
    }
}
