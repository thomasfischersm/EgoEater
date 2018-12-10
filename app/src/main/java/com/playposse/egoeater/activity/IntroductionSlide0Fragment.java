package com.playposse.egoeater.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.glide.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A {@link Fragment} that is shown after the user logs on for the first time. It explains the app
 * to the user.
 */
public class IntroductionSlide0Fragment extends Fragment {

    @BindView(R.id.illustrationImageView) ImageView illustrationImageView;

    public IntroductionSlide0Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(
                R.layout.fragment_introduction_slide0,
                container,
                false);

        ButterKnife.bind(this, rootView);

        GlideApp.with(this)
                .load(R.drawable.infographics0)
                .into(illustrationImageView);

        return rootView;
    }
}
