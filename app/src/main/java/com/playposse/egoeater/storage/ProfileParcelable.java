package com.playposse.egoeater.storage;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.util.DataMunchUtil;
import com.playposse.egoeater.util.IntegerUtil;
import com.playposse.egoeater.util.SmartCursor;
import com.playposse.egoeater.util.StringUtil;

import java.util.List;

import static com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;

/**
 * A {@link Parcelable} that contains the profile information.
 */
public class ProfileParcelable implements Parcelable {

    private static final int NULL_AGE = 0;

    private long profileId;
    private String firstName;
    private String profileText;
    private double distance;
    private String city;
    private String state;
    private String country;
    private int age;
    private String gender;
    private String photoUrl0;
    private String photoUrl1;
    private String photoUrl2;
    private int wins;
    private int losses;
    private int winsLossesSum;

    public static final Parcelable.Creator<ProfileParcelable> CREATOR =
            new Creator<ProfileParcelable>() {
                @Override
                public ProfileParcelable createFromParcel(Parcel source) {
                    return new ProfileParcelable(source);
                }

                @Override
                public ProfileParcelable[] newArray(int size) {
                    return new ProfileParcelable[size];
                }
            };

    public ProfileParcelable(SmartCursor smartCursor) {
        profileId = smartCursor.getLong(ProfileTable.PROFILE_ID_COLUMN);
        firstName = smartCursor.getString(ProfileTable.FIRST_NAME_COLUMN);
        profileText = smartCursor.getString(ProfileTable.PROFILE_TEXT_COLUMN);
        distance = smartCursor.getDouble(ProfileTable.DISTANCE_COLUMN);
        city = smartCursor.getString(ProfileTable.CITY_COLUMN);
        state = smartCursor.getString(ProfileTable.STATE_COLUMN);
        country = smartCursor.getString(ProfileTable.COUNTRY_COLUMN);
        age = smartCursor.getInt(ProfileTable.AGE_COLUMN);
        gender = smartCursor.getString(ProfileTable.GENDER_COLUMN);
        photoUrl0 = smartCursor.getString(ProfileTable.PHOTO_URL_0_COLUMN);
        photoUrl1 = smartCursor.getString(ProfileTable.PHOTO_URL_1_COLUMN);
        photoUrl2 = smartCursor.getString(ProfileTable.PHOTO_URL_2_COLUMN);
        wins = smartCursor.getInt(ProfileTable.WINS_COLUMN);
        losses = smartCursor.getInt(ProfileTable.LOSSES_COLUMN);
        winsLossesSum = smartCursor.getInt(ProfileTable.WINS_LOSSES_SUM_COLUMN);
    }

    public ProfileParcelable(UserBean userBean) {
        profileId = userBean.getUserId();
        firstName = userBean.getFirstName();
        profileText = userBean.getProfileText();
        distance = -1;
        city = userBean.getCity();
        state = userBean.getState();
        country = userBean.getCountry();
        age = IntegerUtil.get(DataMunchUtil.getAge(userBean.getBirthday()), NULL_AGE);
        gender = userBean.getGender();
        if (userBean.getProfilePhotoUrls() != null) {
            if (userBean.getProfilePhotoUrls().size() > 0) {
                photoUrl0 = userBean.getProfilePhotoUrls().get(0);
            }
            if (userBean.getProfilePhotoUrls().size() > 1) {
                photoUrl1 = userBean.getProfilePhotoUrls().get(1);
            }
            if (userBean.getProfilePhotoUrls().size() > 2) {
                photoUrl2 = userBean.getProfilePhotoUrls().get(2);
            }
        }
        wins = 0;
        losses = 0;
        winsLossesSum = 0;
    }

    private ProfileParcelable(Parcel source) {
        profileId = source.readLong();
        firstName = source.readString();
        profileText = source.readString();
        distance = source.readDouble();
        city = source.readString();
        state = source.readString();
        country = source.readString();
        age = source.readInt();
        gender = source.readString();
        photoUrl0 = source.readString();
        photoUrl1 = source.readString();
        photoUrl2 = source.readString();
        wins = source.readInt();
        losses = source.readInt();
        winsLossesSum = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(profileId);
        dest.writeString(firstName);
        dest.writeString(profileText);
        dest.writeDouble(distance);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(country);
        dest.writeInt(age);
        dest.writeString(gender);
        dest.writeString(photoUrl0);
        dest.writeString(photoUrl1);
        dest.writeString(photoUrl2);
        dest.writeInt(wins);
        dest.writeInt(losses);
        dest.writeInt(winsLossesSum);
    }

    public static ContentValues toContentValues(ProfileBean profile) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EgoEaterContract.ProfileTable.PROFILE_ID_COLUMN, profile.getUserId());
        contentValues.put(EgoEaterContract.ProfileTable.FIRST_NAME_COLUMN, profile.getFirstName());
        contentValues.put(EgoEaterContract.ProfileTable.PROFILE_TEXT_COLUMN, StringUtil.trim(profile.getProfileText()));
        contentValues.put(EgoEaterContract.ProfileTable.DISTANCE_COLUMN, profile.getDistance());
        contentValues.put(EgoEaterContract.ProfileTable.CITY_COLUMN, profile.getCity());
        contentValues.put(EgoEaterContract.ProfileTable.STATE_COLUMN, profile.getState());
        contentValues.put(EgoEaterContract.ProfileTable.COUNTRY_COLUMN, profile.getCountry());
        contentValues.put(EgoEaterContract.ProfileTable.AGE_COLUMN, profile.getAge());
        contentValues.put(EgoEaterContract.ProfileTable.GENDER_COLUMN, profile.getGender());

        List<String> profilePhotoUrls = profile.getProfilePhotoUrls();
        if (profilePhotoUrls != null) {
            if (profilePhotoUrls.size() > 0) {
                contentValues.put(
                        EgoEaterContract.ProfileTable.PHOTO_URL_0_COLUMN,
                        profilePhotoUrls.get(0));
            }
            if (profilePhotoUrls.size() > 1) {
                contentValues.put(
                        EgoEaterContract.ProfileTable.PHOTO_URL_1_COLUMN,
                        profilePhotoUrls.get(1));
            }
            if (profilePhotoUrls.size() > 2) {
                contentValues.put(
                        EgoEaterContract.ProfileTable.PHOTO_URL_2_COLUMN,
                        profilePhotoUrls.get(2));
            }
        }
        return contentValues;
    }

    public long getProfileId() {
        return profileId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getProfileText() {
        return profileText;
    }

    public double getDistance() {
        return distance;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getPhotoUrl0() {
        return photoUrl0;
    }

    public String getPhotoUrl1() {
        return photoUrl1;
    }

    public String getPhotoUrl2() {
        return photoUrl2;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getWinsLossesSum() {
        return winsLossesSum;
    }
}
