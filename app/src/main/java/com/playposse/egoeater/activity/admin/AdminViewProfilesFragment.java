package com.playposse.egoeater.activity.admin;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.admin.AdminContract.EgoEaterUserTable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.SmartCursor;
import com.playposse.egoeater.util.StringUtil;
import com.playposse.egoeater.util.ui.RecyclerViewCursorAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An {@link Fragment} that shows an admin all the users.
 */
public class AdminViewProfilesFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 5;

    @BindView(R.id.user_recycler_view) RecyclerView userRecyclerView;

    private UserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_admin_view_profiles,
                container,
                false);

        ButterKnife.bind(this, rootView);

        // Set up RecyclerView.
        userRecyclerView.setHasFixedSize(true); // Small performance improvement.
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        userAdapter = new UserAdapter(getActivity());
        userRecyclerView.setAdapter(userAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                EgoEaterUserTable.CONTENT_URI,
                EgoEaterUserTable.COLUMN_NAMES,
                EgoEaterUserTable.PROFILE_PHOTO_0_COLUMN + " is not null",
                null,
                EgoEaterUserTable.LAST_LOGIN_COLUMN + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        userAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        userAdapter.swapCursor(null);
    }

    /**
     * An {@link Adapter} that shows a list of all the user profiles.
     */
    private class UserAdapter extends RecyclerViewCursorAdapter<UserViewHolder> {

        private final Context context;

        private UserAdapter(Context context) {
            this.context = context;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(
                    R.layout.fragment_admin_view_profile,
                    parent,
                    false);
            return new UserViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(UserViewHolder holder, int position, Cursor cursor) {
            SmartCursor smartCursor = new SmartCursor(cursor, EgoEaterUserTable.COLUMN_NAMES);

            // Load profile photos.
            String photoUrl0 = smartCursor.getString(EgoEaterUserTable.PROFILE_PHOTO_0_COLUMN);
            String photoUrl1 = smartCursor.getString(EgoEaterUserTable.PROFILE_PHOTO_1_COLUMN);
            String photoUrl2 = smartCursor.getString(EgoEaterUserTable.PROFILE_PHOTO_2_COLUMN);

            GlideUtil.load(holder.getProfilePhoto0ImageView(), photoUrl0);
            GlideUtil.load(holder.getProfilePhoto1ImageView(), photoUrl1);
            GlideUtil.load(holder.getProfilePhoto2ImageView(), photoUrl2);

            // Hide show cards for extra photos.
            holder.getPhoto1CardView().setVisibility(
                    StringUtil.isEmpty(photoUrl1) ? View.GONE : View.VISIBLE);
            holder.getPhoto2CardView().setVisibility(
                    StringUtil.isEmpty(photoUrl2) ? View.GONE : View.VISIBLE);

            // Load profile text.
            ProfileParcelable profile = new ProfileParcelable(smartCursor, true);
            String headLine = ProfileFormatter.formatNameAndAge(getContext(), profile);
            holder.getHeadlineTextView().setText(headLine);
            String subHead = ProfileFormatter.formatCityStateDistanceAndProfile(
                    getContext(),
                    profile);
            holder.getSubHeadTextView().setText(subHead);
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} that holds the profile view.
     */
    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.profilePhoto0ImageView) ImageView profilePhoto0ImageView;
        @BindView(R.id.profilePhoto1ImageView) ImageView profilePhoto1ImageView;
        @BindView(R.id.profilePhoto2ImageView) ImageView profilePhoto2ImageView;
        @BindView(R.id.photo1CardView) CardView photo1CardView;
        @BindView(R.id.photo2CardView) CardView photo2CardView;
        @BindView(R.id.headlineTextView) TextView headlineTextView;
        @BindView(R.id.subHeadTextView) TextView subHeadTextView;

        private UserViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public ImageView getProfilePhoto0ImageView() {
            return profilePhoto0ImageView;
        }

        public ImageView getProfilePhoto1ImageView() {
            return profilePhoto1ImageView;
        }

        public ImageView getProfilePhoto2ImageView() {
            return profilePhoto2ImageView;
        }

        public CardView getPhoto1CardView() {
            return photo1CardView;
        }

        public CardView getPhoto2CardView() {
            return photo2CardView;
        }

        public TextView getHeadlineTextView() {
            return headlineTextView;
        }

        public TextView getSubHeadTextView() {
            return subHeadTextView;
        }
    }
}
