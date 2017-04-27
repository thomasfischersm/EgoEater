package com.playposse.egoeater.activity;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.SmartCursor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ViewProfileActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<ProfileParcelable> {

    private static final int LOADER_ID = 2;

    private ImageView profilePhoto0ImageView;
    private ImageView profilePhoto1ImageView;
    private ImageView profilePhoto2ImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;

    private Long profileId;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_view_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profilePhoto0ImageView = (ImageView) findViewById(R.id.profilePhoto0ImageView);
        profilePhoto1ImageView = (ImageView) findViewById(R.id.profilePhoto1ImageView);
        profilePhoto2ImageView = (ImageView) findViewById(R.id.profilePhoto2ImageView);
        headlineTextView = (TextView) findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) findViewById(R.id.subHeadTextView);

        profileId = ExtraConstants.getProfileId(getIntent());

        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        GlideUtil.load(profilePhoto0ImageView, profile.getPhotoUrl0());
        GlideUtil.load(profilePhoto1ImageView, profile.getPhotoUrl1());
        GlideUtil.load(profilePhoto2ImageView, profile.getPhotoUrl2());

        String headline = ProfileFormatter.formatNameAndAge(this, profile);
        setTitle(headline);
        headlineTextView.setText(headline);
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(this, profile));

        profilePhoto1ImageView.setVisibility((profile.getPhotoUrl1() != null) ? VISIBLE : GONE);
        profilePhoto2ImageView.setVisibility((profile.getPhotoUrl2() != null) ? VISIBLE : GONE);
    }

    @Override
    public void onLoaderReset(Loader<ProfileParcelable> loader) {

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
