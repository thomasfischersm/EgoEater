package com.playposse.egoeater.storage;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.util.SmartCursor;

/**
 * A {@link Parcelable} of a pairing for ranking.
 */
public class PairingParcelable implements Parcelable {

    private int pairingId;
    private long profileId0;
    private long profileId1;

    public PairingParcelable(SmartCursor smartCursor) {
        pairingId = smartCursor.getInt(PipelineTable.ID_COLUMN);
        profileId0 = smartCursor.getLong(PipelineTable.PROFILE_0_ID_COLUMN);
        profileId1 = smartCursor.getLong(PipelineTable.PROFILE_1_ID_COLUMN);
    }

    private PairingParcelable(Parcel in) {
        pairingId = in.readInt();
        profileId0 = in.readLong();
        profileId1 = in.readLong();
    }

    public static final Creator<PairingParcelable> CREATOR = new Creator<PairingParcelable>() {
        @Override
        public PairingParcelable createFromParcel(Parcel in) {
            return new PairingParcelable(in);
        }

        @Override
        public PairingParcelable[] newArray(int size) {
            return new PairingParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pairingId);
        dest.writeLong(profileId0);
        dest.writeLong(profileId1);
    }

    public int getPairingId() {
        return pairingId;
    }

    public long getProfileId0() {
        return profileId0;
    }

    public long getProfileId1() {
        return profileId1;
    }
}
