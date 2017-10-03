package com.playposse.egoeater.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playposse.egoeater.R;

/**
 * A {@link Fragment} that stops the user from going to the compare activity because the profile
 * needs to be completed first.
 */
public class ProfileNotReadyFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile_not_ready, container, false);

        return rootView;
    }
}
