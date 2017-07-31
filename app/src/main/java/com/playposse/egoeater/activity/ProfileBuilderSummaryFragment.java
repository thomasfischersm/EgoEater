package com.playposse.egoeater.activity;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.data.profilewizard.ProfileAnswer;
import com.playposse.egoeater.data.profilewizard.ProfileUserData;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;

/**
 * A {@link Fragment} that shows the selected options of all questions and allows the user to
 * re-order the profile with drag'n'drop.
 */
public class ProfileBuilderSummaryFragment extends Fragment {

    private static final String LOG_TAG = ProfileBuilderSummaryFragment.class.getSimpleName();

    private ImageView profilePhotoImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private RecyclerView summaryRecyclerView;

    private ProfileUserData profileUserData;
    private ProfileSummaryAdapter profileSummaryAdapter;

    public ProfileBuilderSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView =
                inflater.inflate(R.layout.fragment_profile_builder_summary, container, false);

        profilePhotoImageView = (ImageView) rootView.findViewById(R.id.profilePhotoImageView);
        summaryRecyclerView = (RecyclerView) rootView.findViewById(R.id.summaryRecyclerView);
        headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);

        Fragment parentFragment = getFragmentManager().findFragmentById(R.id.mainFragmentContainer);
//        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ProfileBuilderFragment) {
            ProfileBuilderFragment profileBuilderFragment = (ProfileBuilderFragment) parentFragment;
            profileUserData = profileBuilderFragment.getProfileUserData();
            profileUserData.refreshOrder();
        }

        summaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        profileSummaryAdapter = new ProfileSummaryAdapter();
        summaryRecyclerView.setAdapter(profileSummaryAdapter);

        String photoUrl = EgoEaterPreferences.getProfilePhotoUrl1(getActivity());
        GlideUtil.load(profilePhotoImageView, photoUrl);

        refreshPreview();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && (profileUserData != null) && (profileSummaryAdapter != null)) {
            profileUserData.refreshOrder();
            profileSummaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the data  for the profile text preview. This lets the user see approximately how much of
     * the profile text will be visible.
     */
    private void refreshPreview() {
        UserBean userBean = EgoEaterPreferences.getUser(getContext());
        userBean.setProfileText(profileUserData.toString(getContext()));
        ProfileParcelable profile = new ProfileParcelable(userBean);

        headlineTextView.setText(ProfileFormatter.formatNameAndAge(getContext(), profile));
        subHeadTextView.setText(
                ProfileFormatter.formatCityStateDistanceAndProfile(getContext(), profile));
    }

    private void moveSummary(int fromPosition, int toPosition) {
        profileUserData.move(fromPosition, toPosition);
        profileSummaryAdapter.notifyDataSetChanged();
        refreshPreview();
    }

    private int getAvailableDragTargetTint() {
        return ContextCompat.getColor(getContext(), R.color.availableDragTargetTint);
    }

    private int getActiveDragTargetTint() {
        return ContextCompat.getColor(getContext(), R.color.activeDragTargetTint);
    }

    /**
     * A {@link RecyclerView.Adapter} that allows the user to re-order the answers to questions.
     */
    class ProfileSummaryAdapter
            extends RecyclerView.Adapter<ProfileSummaryAdapter.ProfileSummaryViewHolder> {

        @Override
        public ProfileSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            TextView v =
                    (TextView) inflater.inflate(R.layout.profile_summary_list_item, parent, false);
            return new ProfileSummaryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ProfileSummaryViewHolder holder, int position) {
            Integer questionIndex = profileUserData.getAnswersOrder().get(position);
            ProfileAnswer answer = profileUserData.getAnswer(questionIndex);
            String optionsStr = answer.toString(getContext());
            TextView textView = holder.getTextView();
            textView.setText(optionsStr);

            // Initiate drag'n'drop.
            textView.setOnTouchListener(new StartDragTouchListener(textView, position));
            textView.setOnDragListener(new SummaryDragListener(position));
        }

        @Override
        public int getItemCount() {
            return profileUserData.getAnswersOrder().size();
        }

        class ProfileSummaryViewHolder extends RecyclerView.ViewHolder {

            private TextView textView;

            ProfileSummaryViewHolder(TextView textView) {
                super(textView);

                this.textView = textView;
            }

            TextView getTextView() {
                return textView;
            }
        }
    }

    /**
     * An {@link View.OnTouchListener} that starts the drag'n'drop process.
     */
    class StartDragTouchListener implements View.OnTouchListener {

        private final TextView textView;
        private final int position;

        StartDragTouchListener(TextView textView, int position) {
            this.textView = textView;
            this.position = position;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", Integer.toString(position));
                View.DragShadowBuilder shadowBuilder = new TextViewDragShadowBuilder(textView);
                textView.startDrag(data, shadowBuilder, textView, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * A {@link View.OnDragListener} that let's the {@link TextView} be dragged to a new position.
     */
    class SummaryDragListener implements View.OnDragListener {

        private final int position;

        SummaryDragListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onDrag(View view, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setBackgroundColor(getAvailableDragTargetTint());
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    view.setBackgroundColor(getActiveDragTargetTint());
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    // Ignore the event
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    view.setBackgroundColor(getAvailableDragTargetTint());
                    return true;
                case DragEvent.ACTION_DROP:
                    String fromPositionStr = event.getClipData().getItemAt(0).getText().toString();
                    int fromPosition = Integer.parseInt(fromPositionStr);
                    moveSummary(fromPosition, position);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    view.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                default:
                    Log.e(LOG_TAG, "onDrag: Unknown drag action type: " + event.getAction());
                    return false;
            }

        }
    }

    /**
     * A {@link View.DragShadowBuilder} that Sets the touch point to the left side of the dragged
     * view. The text doesn't fill the whole width. So, if a user touches pretty far left, the text
     * may show up left of the screen. The user will not know which text he or she is dragging.
     */
    private class TextViewDragShadowBuilder extends View.DragShadowBuilder {

        private TextViewDragShadowBuilder(View view) {
            super(view);
        }

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);

            outShadowTouchPoint.set(0, outShadowSize.y / 2);
        }
    }
}
