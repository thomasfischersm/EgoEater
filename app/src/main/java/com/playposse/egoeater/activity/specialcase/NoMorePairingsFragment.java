package com.playposse.egoeater.activity.specialcase;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playposse.egoeater.R;

/**
 * A {@link Fragment} that shows the "no more pairings message."
 */
public class NoMorePairingsFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_no_more_pairings, container, false);

        return rootView;
    }
}
