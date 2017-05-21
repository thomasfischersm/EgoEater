package com.playposse.egoeater.activity;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;

/**
 * An {@link Activity} that shows introductory slides to the user after the first user log on.
 */
public class IntroductionActivity extends AppCompatActivity {

    private static final String LOG_TAG = IntroductionActivity.class.getSimpleName();

    private ViewPager introductionSlidePager;
    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_introduction);

        introductionSlidePager = (ViewPager) findViewById(R.id.introductionSlidePager);
        getStartedButton = (Button) findViewById(R.id.getStartedButton);

        IntroductionSlidePagerAdapter pagerAdapter =
                new IntroductionSlidePagerAdapter(getSupportFragmentManager());
        introductionSlidePager.setAdapter(pagerAdapter);

        introductionSlidePager.addOnPageChangeListener(new AnalyticsPageChangeListener());

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EgoEaterPreferences.setHasSeenIntroDeck(getApplicationContext(), true);
                GlobalRouting.onLoginComplete(getApplicationContext());
            }
        });
    }

    private class IntroductionSlidePagerAdapter extends FragmentPagerAdapter {

        public IntroductionSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new IntroductionSlide0Fragment();
                case 1:
                    return new IntroductionSlide1Fragment();
                case 2:
                    return new IntroductionSlide2Fragment();
                default:
                    throw new IllegalStateException(
                            "Unexpected introduction deck was requested: " + position);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    /**
     * A {@link android.support.v4.view.ViewPager.OnPageChangeListener} that reports to Analytics
     * when a new fragment is selected.
     */
    private class AnalyticsPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Nothing to do.
        }

        @Override
        public void onPageSelected(int position) {
            String screenName = IntroductionActivity.class.getSimpleName();
            AnalyticsUtil.reportScreenName(getApplication(), screenName);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Nothing to do.
        }
    }
}
