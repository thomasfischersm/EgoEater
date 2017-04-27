package com.playposse.egoeater.activity;

import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchAndProfileQuery;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.storage.MatchParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.NotificationUtil;
import com.playposse.egoeater.util.NotificationUtil.NotificationType;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.RecyclerViewCursorAdapter;
import com.playposse.egoeater.util.SimpleAlertDialog;
import com.playposse.egoeater.util.SmartCursor;

import static android.support.v7.widget.RecyclerView.*;

public class MatchesActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // TODO: Think about listening to contentprovider changes and refreshing the view.

    private static final String LOG_TAG = MatchesActivity.class.getSimpleName();

    private static final int COLUMN_COUNT = 3;
    private static final int LOADER_ID = 1;

    private RecyclerView matchesRecyclerView;
    private TextView noMatchesTextView;

    private MatchesCursorAdapter matchesCursorAdapter;
    private ContentObserver contentObserver;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_matches;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.matches_activity_title);

        matchesRecyclerView = (RecyclerView) findViewById(R.id.matchesRecyclerView);
        noMatchesTextView = (TextView) findViewById(R.id.noMatchesTextView);

        matchesRecyclerView.setLayoutManager(new GridLayoutManager(this, COLUMN_COUNT));
        matchesCursorAdapter = new MatchesCursorAdapter();
        matchesRecyclerView.setAdapter(matchesCursorAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the view when new messages arrive.
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(LOG_TAG, "onChange: Noticed that the mtach table has changed.");
                matchesCursorAdapter.notifyDataSetChanged();
            }
        };
        getContentResolver().registerContentObserver(
                EgoEaterContract.MatchTable.CONTENT_URI,
                false,
                contentObserver);

        NotificationUtil.clear(this, NotificationType.UpdatedMatches);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (contentObserver != null) {
            getContentResolver().unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                MatchAndProfileQuery.CONTENT_URI,
                MatchAndProfileQuery.COLUMN_NAMES,
                null,
                null,
                MatchTable.IS_LOCKED_COLUMN + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        matchesCursorAdapter.swapCursor(cursor);
        matchesRecyclerView.setVisibility((cursor.getCount() > 0) ? VISIBLE : GONE);
        noMatchesTextView.setVisibility((cursor.getCount() > 0) ? GONE : VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        matchesCursorAdapter.swapCursor(null);
        matchesRecyclerView.setVisibility(GONE);
        noMatchesTextView.setVisibility(VISIBLE);
    }

    /**
     * A cursor adaptor for matches.
     */
    private class MatchesCursorAdapter extends RecyclerViewCursorAdapter<MatchesViewHolder> {

        @Override
        public MatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view =
                    LayoutInflater.from(context).inflate(R.layout.matches_list_item, parent, false);
            return new MatchesViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(MatchesViewHolder holder, int position, Cursor cursor) {
            // Load data out of cursor.
            SmartCursor smartCursor = new SmartCursor(cursor, MatchAndProfileQuery.COLUMN_NAMES);
            MatchParcelable match = new MatchParcelable(smartCursor);
            ProfileParcelable profile = match.getOtherProfile();
            final long profileId = profile.getProfileId();
            final boolean isLocked = match.isLocked();

            // Load profile photo.
            Glide.with(getApplicationContext())
                    .load(profile.getPhotoUrl0())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .into(holder.getProfileImageView());

            // Populate the rest of the profile snapshot.
            if (isLocked) {
                holder.getLockIconImageView().setImageResource(R.drawable.ic_lock_black_24dp);
            } else {
                holder.getLockIconImageView().setImageResource(R.drawable.ic_lock_open_black_24dp);
            }
            String headLine = ProfileFormatter.formatNameAndAge(getApplicationContext(), profile);
            holder.getHeadlineTextView().setText(headLine);
            String subHead =
                    ProfileFormatter.formatCityStateAndDistance(getApplicationContext(), profile);
            holder.getSubHeadTextView().setText(subHead);

            // Add click listeners.
            holder.getProfileImageView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExtraConstants.startMessagesActivity(getApplicationContext(), profileId);
                }
            });

            holder.getLockIconImageView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleAlertDialog.show(
                            MatchesActivity.this,
                            R.string.match_lock_icon_title,
                            R.string.match_lock_icon_explanation);
                }
            });
        }
    }

    /**
     * {@link ViewHolder} for matches.
     */
    private static class MatchesViewHolder extends ViewHolder {

        private final ImageView profileImageView;
        private final ImageView lockIconImageView;
        private final TextView headlineTextView;
        private final TextView subHeadTextView;

        public MatchesViewHolder(View itemView) {
            super(itemView);

            profileImageView = (ImageView) itemView.findViewById(R.id.profileImageView);
            lockIconImageView = (ImageView) itemView.findViewById(R.id.lockIconImageView);
            headlineTextView = (TextView) itemView.findViewById(R.id.headlineTextView);
            subHeadTextView = (TextView) itemView.findViewById(R.id.subHeadTextView);
        }

        public ImageView getProfileImageView() {
            return profileImageView;
        }

        public ImageView getLockIconImageView() {
            return lockIconImageView;
        }

        public TextView getHeadlineTextView() {
            return headlineTextView;
        }

        public TextView getSubHeadTextView() {
            return subHeadTextView;
        }
    }
}
