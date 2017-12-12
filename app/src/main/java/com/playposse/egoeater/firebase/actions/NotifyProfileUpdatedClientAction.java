package com.playposse.egoeater.firebase.actions;

import android.content.ContentResolver;
import android.content.ContentValues;

import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.GetProfilesByIdClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.firebase.FirebaseMessage;
import com.playposse.egoeater.util.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * A Firebase client action that is triggered when another user has updated his/her profile.
 */
public class NotifyProfileUpdatedClientAction extends FirebaseClientAction {

    private static final String PROFILE_ID_KEY = "senderProfileId";

    public NotifyProfileUpdatedClientAction(RemoteMessage remoteMessage) {
        super(remoteMessage);
    }

    @Override
    protected void execute(RemoteMessage remoteMessage) {
        NotifyProfileUpdatedMessage message = new NotifyProfileUpdatedMessage(remoteMessage);
        final long profileId = message.getProfileId();

        new GetProfilesByIdClientAction(
                getApplicationContext(),
                new ApiClientAction.Callback<List<ProfileBean>>() {
                    @Override
                    public void onResult(List<ProfileBean> profileBeans) {
                        if ((profileBeans != null) && (profileBeans.size() > 0)) {
                            updateProfile(profileId, profileBeans.get(0));
                        }
                    }
                },
                Collections.singletonList(profileId))
                .execute();
    }

    private void updateProfile(long profileId, ProfileBean profileBean) {
        // Prepare photo URLs.
        String photoUrl0 = null;
        String photoUrl1 = null;
        String photoUrl2 = null;
        if (profileBean.getProfilePhotoUrls() != null) {
            if (profileBean.getProfilePhotoUrls().size() > 0) {
                photoUrl0 = profileBean.getProfilePhotoUrls().get(0);
            }
            if (profileBean.getProfilePhotoUrls().size() > 1) {
                photoUrl1 = profileBean.getProfilePhotoUrls().get(1);
            }
            if (profileBean.getProfilePhotoUrls().size() > 2) {
                photoUrl2 = profileBean.getProfilePhotoUrls().get(2);
            }
        }

        // Create ContentValues.
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProfileTable.IS_ACTIVE_COLUMN, profileBean.getActive());
        contentValues.put(ProfileTable.FIRST_NAME_COLUMN, profileBean.getFirstName());
        contentValues.put(ProfileTable.PROFILE_TEXT_COLUMN, StringUtil.trim(profileBean.getProfileText()));
        contentValues.put(ProfileTable.DISTANCE_COLUMN, profileBean.getDistance());
        contentValues.put(ProfileTable.CITY_COLUMN, profileBean.getCity());
        contentValues.put(ProfileTable.STATE_COLUMN, profileBean.getState());
        contentValues.put(ProfileTable.COUNTRY_COLUMN, profileBean.getCountry());
        contentValues.put(ProfileTable.AGE_COLUMN, profileBean.getAge());
        contentValues.put(ProfileTable.GENDER_COLUMN, profileBean.getGender());
        contentValues.put(ProfileTable.PHOTO_URL_0_COLUMN, photoUrl0);
        contentValues.put(ProfileTable.PHOTO_URL_1_COLUMN, photoUrl1);
        contentValues.put(ProfileTable.PHOTO_URL_2_COLUMN, photoUrl2);

        // Submit the update to the database.
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.update(
                ProfileTable.CONTENT_URI,
                contentValues,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(profileId)});
    }

    /**
     * A Firebase message that carries information about another user's profile, which has been
     * updated.
     */
    private static final class NotifyProfileUpdatedMessage extends FirebaseMessage {

        private NotifyProfileUpdatedMessage(RemoteMessage message) {
            super(message);
        }

        private long getProfileId() {
            return getLong(PROFILE_ID_KEY);
        }
    }
}
