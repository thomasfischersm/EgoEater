package com.playposse.egoeater.activity;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.FuckOffUiHelper;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.SimpleAlertDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ViewProfileActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<ProfileParcelable> {

    private static final String LOG_TAG = ViewProfileActivity.class.getSimpleName();

    private static final int LOADER_ID = 2;

    private ImageView profilePhoto0ImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private LinearLayout thumbnailLayout;
    private CardView thumbnail1CardView;
    private ImageView profilePhoto1ImageView;
    private CardView thumbnail2CardView;
    private ImageView profilePhoto2ImageView;
    private TextView profileTextView;
    private ImageView fuckOffImageView;
    private TextView fuckOffTextView;
    private ImageView reportImageView;
    private TextView reportTextView;
    private FloatingActionButton messagingButton;

    private Long profileId;
    private int mainImageIndex = 0;
    private ProfileParcelable profile;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_view_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get references to views.
        profilePhoto0ImageView = (ImageView) findViewById(R.id.profilePhoto0ImageView);
        headlineTextView = (TextView) findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) findViewById(R.id.subHeadTextView);
        thumbnailLayout = (LinearLayout) findViewById(R.id.thumbnailLayout);
        thumbnail1CardView = (CardView) findViewById(R.id.thumbnail1CardView);
        profilePhoto1ImageView = (ImageView) findViewById(R.id.profilePhoto1ImageView);
        thumbnail2CardView = (CardView) findViewById(R.id.thumbnail2CardView);
        profilePhoto2ImageView = (ImageView) findViewById(R.id.profilePhoto2ImageView);
        profileTextView = (TextView) findViewById(R.id.profileTextView);
        fuckOffImageView = (ImageView) findViewById(R.id.fuckOffImageView);
        fuckOffTextView = (TextView) findViewById(R.id.fuckOffTextView);
        reportImageView = (ImageView) findViewById(R.id.reportImageView);
        reportTextView = (TextView) findViewById(R.id.reportTextView);
        messagingButton = (FloatingActionButton) findViewById(R.id.messagingButton);

        profileId = ExtraConstants.getProfileId(getIntent());

        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(R.string.view_profile_title);

        messagingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraConstants.startMessagesActivity(getApplicationContext(), profileId);
            }
        });

        // Set OnClickListeners for the fuck off action.
        View.OnClickListener fuckOffClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FuckOffUiHelper.fuckOff(ViewProfileActivity.this, profileId, getApplication());
            }
        };
        fuckOffImageView.setOnClickListener(fuckOffClickListener);
        fuckOffTextView.setOnClickListener(fuckOffClickListener);

        // Set OnClickListeners for the report abuse action.
        View.OnClickListener reportClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleAlertDialog.showReportAbuseDialog(ViewProfileActivity.this, profileId);
            }
        };
        reportImageView.setOnClickListener(reportClickListener);
        reportTextView.setOnClickListener(reportClickListener);

        // Change main profile photo.
        profilePhoto1ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainImageIndex = (mainImageIndex == 1) ? 0 : 1;
                loadImages();
            }
        });

        // Change main profile photo.
        profilePhoto2ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainImageIndex = (mainImageIndex == 2) ? 0 : 2;
                loadImages();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ExtraConstants.startMessagesActivity(this, profileId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ExtraConstants.startMessagesActivity(this, profileId);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<ProfileParcelable> onCreateLoader(int id, Bundle args) {
        return new ProfileLoader(this, profileId);
    }

    @Override
    public void onLoadFinished(Loader<ProfileParcelable> loader, ProfileParcelable profile) {
        this.profile = profile;

        GlideUtil.load(profilePhoto0ImageView, profile.getPhotoUrl0());
        GlideUtil.load(profilePhoto1ImageView, profile.getPhotoUrl1());
        GlideUtil.load(profilePhoto2ImageView, profile.getPhotoUrl2());

        String headline = ProfileFormatter.formatNameAndAge(this, profile);
        headlineTextView.setText(headline);
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(this, profile));
        profileTextView.setText(profile.getProfileText());

        boolean hasPhoto1 = profile.getPhotoUrl1() != null;
        boolean hasPhoto2 = profile.getPhotoUrl2() != null;
        if (hasPhoto1 || hasPhoto2) {
            thumbnail1CardView.setVisibility(hasPhoto1 ? VISIBLE : GONE);
            thumbnail2CardView.setVisibility(hasPhoto2 ? VISIBLE : GONE);
        } else {
            thumbnailLayout.setVisibility(GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ProfileParcelable> loader) {

    }

    private void loadImages() {
        switch (mainImageIndex) {
            case 0:
                loadImage(0, 0);
                loadImage(1, 1);
                loadImage(2, 2);
                break;
            case 1:
                loadImage(0, 1);
                loadImage(1, 0);
                loadImage(2, 2);
                break;
            case 2:
                loadImage(0, 2);
                loadImage(1, 1);
                loadImage(2, 0);
                break;
            default:
                Log.e(LOG_TAG, "loadImages: Unexpected mainImageIndex: " + mainImageIndex);
                break;
        }
    }

    private void loadImage(int slotIndex, int photoIndex) {
        final String photoUrl;
        switch (photoIndex) {
            case 0:
                photoUrl = profile.getPhotoUrl0();
                break;
            case 1:
                photoUrl = profile.getPhotoUrl1();
                break;
            case 2:
                photoUrl = profile.getPhotoUrl2();
                break;
            default:
                Log.e(LOG_TAG, "loadImage: Unexpected photoIndex: " + photoIndex);
                return;
        }

        final ImageView imageView;
        switch (slotIndex) {
            case 0:
                imageView = profilePhoto0ImageView;
                break;
            case 1:
                imageView = profilePhoto1ImageView;
                break;
            case 2:
                imageView = profilePhoto2ImageView;
                break;
            default:
                Log.e(LOG_TAG, "loadImage: Unexpected slotIndex: " + slotIndex);
                return;
        }

        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (photoUrl != null) {
                    imageView.setVisibility(View.VISIBLE);
                    GlideUtil.load(imageView, photoUrl);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * A loader that retrieves the profile from the content provider.
     */
    private static class ProfileLoader extends AsyncTaskLoader<ProfileParcelable> {

        private final long profileId;

        public ProfileLoader(Context context, long profileId) {
            super(context);

            this.profileId = profileId;
        }

        @Override
        public ProfileParcelable loadInBackground() {
            return QueryUtil.getProfileById(getContext().getContentResolver(), profileId);
        }
    }
}
