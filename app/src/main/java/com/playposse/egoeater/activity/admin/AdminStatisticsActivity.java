package com.playposse.egoeater.activity.admin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.ParentActivity;
import com.playposse.egoeater.backend.egoEaterApi.model.AdminStatisticsBean;
import com.playposse.egoeater.clientactions.GetAdminStatisticsClientAction;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An {@link Activity} that shows an admin statistics of the user population.
 */
public class AdminStatisticsActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<AdminStatisticsBean> {

    private static final String LOG_TAG = AdminStatisticsActivity.class.getSimpleName();

    private static final int LOADER_ID = 4;

    @BindView(R.id.total_user_count_text_view) TextView totalUserCountTextView;
    @BindView(R.id.active_user_count_text_view) TextView activeUserCountTextView;
    @BindView(R.id.rating_count_text_view) TextView ratingCountTextView;
    @BindView(R.id.match_count_text_view) TextView matchCountTextView;
    @BindView(R.id.conversation_count_text_view) TextView conversationCountTextView;
    @BindView(R.id.report_duration_text_view) TextView reportDurationTextView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_admin_statistics;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }

    @Override
    public Loader<AdminStatisticsBean> onCreateLoader(int id, Bundle args) {
        return new AdminStatisticsBeanAsyncTaskLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AdminStatisticsBean> loader, AdminStatisticsBean data) {
        if (data == null) {
            // Ignore.
            return;
        }

        totalUserCountTextView.setText(String.format(
                Locale.getDefault(),
                "%1$d",
                data.getTotalUserCount()));

        activeUserCountTextView.setText(String.format(
                Locale.getDefault(),
                "%1$d",
                data.getActiveUserCount()));

        ratingCountTextView.setText(String.format(
                Locale.getDefault(),
                "%1$d",
                data.getRatingsCount()));

        matchCountTextView.setText(String.format(
                Locale.getDefault(),
                "%1$d",
                data.getMatchesCount()));

        conversationCountTextView.setText(String.format(
                Locale.getDefault(),
                "%1$d",
                data.getConversationCount()));

        reportDurationTextView.setText(String.format(
                Locale.getDefault(),
                "%1$d",
                data.getReportDuration()));
    }

    @Override
    public void onLoaderReset(Loader<AdminStatisticsBean> loader) {

    }

    /**
     * An {@link AsyncTaskLoader} that loads the {@link AdminStatisticsBean} from the cloud.
     */
    private static class AdminStatisticsBeanAsyncTaskLoader
            extends AsyncTaskLoader<AdminStatisticsBean> {

        private AdminStatisticsBeanAsyncTaskLoader(Context context) {
            super(context);
        }

        @Nullable
        @Override
        public AdminStatisticsBean loadInBackground() {
            try {
                return new GetAdminStatisticsClientAction(getContext(), null).executeBlocking();
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "loadInBackground: Failed to get admin statistics.", ex);
                Crashlytics.logException(ex);

                return null;
            }
        }
    }
}
