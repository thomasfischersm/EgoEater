package com.playposse.egoeater.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.util.SmartCursor;

import static com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;

/**
 * A {@link Parcelable} that contains the profile information.
 */
public class ProfileParcelable implements Parcelable {

    private long profileId;
    private String firstName;
    private String lastName;
    private String name;
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
        lastName = smartCursor.getString(ProfileTable.LAST_NAME_COLUMN);
        name = smartCursor.getString(ProfileTable.NAME_COLUMN);
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

    private ProfileParcelable(Parcel source) {
        profileId = source.readLong();
        firstName = source.readString();
        lastName = source.readString();
        name = source.readString();
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
        dest.writeString(lastName);
        dest.writeString(name);
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

    public long getProfileId() {
        return profileId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
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
