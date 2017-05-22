package com.playposse.egoeater.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.storage.MatchParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.NotificationUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.RecyclerViewCursorAdapter;
import com.playposse.egoeater.util.ResponsiveGridLayoutManager;
import com.playposse.egoeater.util.SimpleAlertDialog;
import com.playposse.egoeater.util.SmartCursor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * A {@link Fragment} that shows the matches.
 */
public class MatchesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MatchesFragment.class.getSimpleName();

    private static final int MIN_ITEM_DP_WIDTH = 150;
    private static final int MAX_COLUMN_COUNT = 3;
    private static final int LOADER_ID = 1;

    private RecyclerView matchesRecyclerView;
    private LinearLayout noMatchesLayout;

    private MatchesCursorAdapter matchesCursorAdapter;
    private ContentObserver contentObserver;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        matchesRecyclerView = (RecyclerView) rootView.findViewById(R.id.matchesRecyclerView);
        noMatchesLayout = (LinearLayout) rootView.findViewById(R.id.noMatchesLayout);

        ResponsiveGridLayoutManager recycleLayoutManager =
                new ResponsiveGridLayoutManager(getContext(), MIN_ITEM_DP_WIDTH, MAX_COLUMN_COUNT);
        matchesRecyclerView.setLayoutManager(recycleLayoutManager);
        matchesCursorAdapter = new MatchesCursorAdapter();
        matchesRecyclerView.setAdapter(matchesCursorAdapter);

        getActivity().getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the view when new messages arrive.
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(LOG_TAG, "onChange: Noticed that the mtach table has changed.");
                matchesCursorAdapter.notifyDataSetChanged();
            }
        };
        getContext().getContentResolver().registerContentObserver(
                EgoEaterContract.MatchTable.CONTENT_URI,
                false,
                contentObserver);

        NotificationUtil.clear(getContext(), NotificationUtil.NotificationType.UpdatedMatches);
        NotificationUtil.clear(getContext(), NotificationUtil.NotificationType.NewMessage);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (contentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                EgoEaterContract.MatchAndProfileQuery.CONTENT_URI,
                EgoEaterContract.MatchAndProfileQuery.COLUMN_NAMES,
                null,
                null,
                EgoEaterContract.MatchTable.IS_LOCKED_COLUMN + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        matchesCursorAdapter.swapCursor(cursor);
        matchesRecyclerView.setVisibility((cursor.getCount() > 0) ? VISIBLE : GONE);
        noMatchesLayout.setVisibility((cursor.getCount() > 0) ? GONE : VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        matchesCursorAdapter.swapCursor(null);
        matchesRecyclerView.setVisibility(GONE);
        noMatchesLayout.setVisibility(VISIBLE);
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
            SmartCursor smartCursor =
                    new SmartCursor(cursor, EgoEaterContract.MatchAndProfileQuery.COLUMN_NAMES);
            MatchParcelable match = new MatchParcelable(smartCursor);
            ProfileParcelable profile = match.getOtherProfile();
            final long profileId = profile.getProfileId();
            boolean isLocked = match.isLocked();

            // Load profile photo.
            GlideUtil.load(holder.getProfilePhotoImageView(), profile.getPhotoUrl0());

            // Populate the rest of the profile snapshot.
            if (isLocked && !match.hasNewMessage()) {
                holder.getLockIconImageView().setVisibility(VISIBLE);
            } else {
                holder.getLockIconImageView().setVisibility(GONE);
            }
            holder.getNewMessagesLayout().setVisibility(match.hasNewMessage() ? VISIBLE : GONE);
            if (match.getUnreadMessagesCount() > 1) {
                holder.getNewMessagesTextView().setText("" + match.getUnreadMessagesCount());
            } else {
                holder.getNewMessagesTextView().setText("");
            }
            String headLine = ProfileFormatter.formatNameAndAge(getContext(), profile);
            holder.getHeadlineTextView().setText(headLine);
            String subHead =
                    ProfileFormatter.formatCityStateAndDistance(getContext(), profile);
            holder.getSubHeadTextView().setText(subHead);

            // Add click listeners.
            holder.getProfilePhotoImageView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExtraConstants.startMessagesActivity(getContext(), profileId);
                }
            });

            holder.getLockIconImageView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleAlertDialog.alert(
                            getContext(),
                            R.string.match_lock_icon_title,
                            R.string.match_lock_icon_explanation);
                }
            });
        }
    }

    /**
     * {@link RecyclerView.ViewHolder} for matches.
     */
    private static class MatchesViewHolder extends RecyclerView.ViewHolder {

        private final ImageView profilePhotoImageView;
        private final ImageView lockIconImageView;
        private final FrameLayout newMessagesLayout;
        private final ImageView newMessagesImageIcon;
        private final TextView newMessagesTextView;
        private final TextView headlineTextView;
        private final TextView subHeadTextView;

        private MatchesViewHolder(View itemView) {
            super(itemView);

            profilePhotoImageView = (ImageView) itemView.findViewById(R.id.profilePhotoImageView);
            lockIconImageView = (ImageView) itemView.findViewById(R.id.lockIconImageView);
            newMessagesLayout = (FrameLayout) itemView.findViewById(R.id.newMessagesLayout);
            newMessagesImageIcon = (ImageView) itemView.findViewById(R.id.newMessagesImageIcon);
            newMessagesTextView = (TextView) itemView.findViewById(R.id.newMessagesTextView);
            headlineTextView = (TextView) itemView.findViewById(R.id.headlineTextView);
            subHeadTextView = (TextView) itemView.findViewById(R.id.subHeadTextView);
        }

        private ImageView getProfilePhotoImageView() {
            return profilePhotoImageView;
        }

        private ImageView getLockIconImageView() {
            return lockIconImageView;
        }

        private FrameLayout getNewMessagesLayout() {
            return newMessagesLayout;
        }

        private ImageView getNewMessagesImageIcon() {
            return newMessagesImageIcon;
        }

        private TextView getNewMessagesTextView() {
            return newMessagesTextView;
        }

        private TextView getHeadlineTextView() {
            return headlineTextView;
        }

        private TextView getSubHeadTextView() {
            return subHeadTextView;
        }
    }
}
