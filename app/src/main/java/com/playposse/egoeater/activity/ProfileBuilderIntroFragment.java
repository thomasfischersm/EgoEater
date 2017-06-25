package com.playposse.egoeater.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playposse.egoeater.R;

/**
 * An introduction slide in the {@link ProfileBuilderActivity} that explains the user the profile
 * builder wizard.
 */
public class ProfileBuilderIntroFragment extends Fragment {


    public ProfileBuilderIntroFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile_builder_intro, container, false);
    }
}
