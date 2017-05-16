package com.playposse.egoeater.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;

/**
 * A {@link Fragment} that shows a profile in the {@link RatingActivity}.
 */
public class RatingProfileFragment extends Fragment {

    private static final String LOG_TAG = RatingProfileFragment.class.getSimpleName();

    private static final String PROFILE_PARAM = "profile";

    private ProfileParcelable profile;
    private ProfileSelectionListener listener;
    private int mainImageIndex = 0;

    private ImageView profilePhoto0ImageView;
    private CardView photo1CardView;
    private ImageView profilePhoto1ImageView;
    private CardView photo2CardView;
    private ImageView profilePhoto2ImageView;
    private ImageView heartImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;

    public RatingProfileFragment() {
        // Required empty public constructor
    }

    public void setProfile(ProfileParcelable profile) {
        this.profile = profile;

        refreshView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profile = getArguments().getParcelable(PROFILE_PARAM);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_rating_profile, container, false);

        profilePhoto0ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto0ImageView);
        photo1CardView = (CardView) rootView.findViewById(R.id.photo1CardView);
        profilePhoto1ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto1ImageView);
        photo2CardView = (CardView) rootView.findViewById(R.id.photo2CardView);
        profilePhoto2ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto2ImageView);
        heartImageView = (ImageView) rootView.findViewById(R.id.heartImageView);
        headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);

        refreshView();

        // Select profile.
        profilePhoto0ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onProfileSelected(profile);
                }
            }
        });

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

        profilePhoto0ImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        heartImageView.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        heartImageView.setVisibility(View.GONE);
                        break;
                }

                // Never consume the event. Simply show/hide the heart icon.
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PROFILE_PARAM, profile);
    }

    private void refreshView() {
        mainImageIndex = 0;
        if (profile != null) {
            loadImages();
            String headLine = ProfileFormatter.formatNameAndAge(getContext(), profile);
            headlineTextView.setText(headLine);
            String subHead = ProfileFormatter.formatCityStateDistanceAndProfile(
                    getContext(),
                    profile);
            subHeadTextView.setText(subHead);
        }
    }

    private void loadImages() {
        if (profile != null) {
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

        // Hide extra image slots
        if (profile.getPhotoUrl1() == null) {
            photo1CardView.setVisibility(View.GONE);
        }
        if (profile.getPhotoUrl2() == null) {
            photo2CardView.setVisibility(View.GONE);
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
                    GlideUtil.load(imageView, photoUrl, R.drawable.ic_insert_emoticon_black_24dp);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileSelectionListener) {
            listener = (ProfileSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ProfileSelectionListener {
        void onProfileSelected(ProfileParcelable profile);
    }
}
