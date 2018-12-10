package com.playposse.egoeater.util;

import androidx.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.contentprovider.admin.AdminContract.EgoEaterUserTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Utility that transforms raw data from Facebook and other sources into privacy sensitive data
 * that can be displayed to the user.
 */
public class DataMunchUtil {

    private static final String LOG_TAG = DataMunchUtil.class.getSimpleName();

    public static final int NULL_AGE = 0;

    private static final String FULL_FB_DATE_FORMAT = "MM/dd/yyyy";
    private static final char DATE_SEPARATOR = '/';

    /**
     * Calculates the birthday based on the FB provided information. If it cannot be determined,
     * null is returned.
     */
    @Nullable
    public static Integer getAge(String birthday) {
        if (StringUtil.isEmpty(birthday)) {
            return null;
        }

        String[] dateParts = birthday.split(birthday, DATE_SEPARATOR);
        try {
            switch (dateParts.length) {
                case 2:
                    // A fully formed date: mm/dd/yyyy
                    Calendar now = Calendar.getInstance();
                    Calendar dob = Calendar.getInstance();
                    dob.setTime(new SimpleDateFormat(FULL_FB_DATE_FORMAT, Locale.US).parse(birthday));

                    int age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                    if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    return age;
                case 1:
                    // Only a day and month: mm/dd. Can't determine the age.
                    return null;
                case 0:
                    // Only a year: yyyy
                    Calendar today = Calendar.getInstance();
                    int year = Integer.parseInt(birthday);
                    // Note, this may be off by a year because we the exact day is unknown.
                    return today.get(Calendar.YEAR) - year;
                default:
                    // Unexpected format. Give up!
                    return null;
            }
        } catch (ParseException ex) {
            Log.e(LOG_TAG, "getAge: Couldn't parse FB date: " + birthday, ex);
            Crashlytics.logException(ex);
            return null;
        }
    }

    public static int getAgeFromEgoEaterUserTable(SmartCursor smartCursor) {
        Integer age = getAge(smartCursor.getString(EgoEaterUserTable.BIRTHDAY_OVERRIDE_COLUMN));

        if (age == null) {
            age = getAge(smartCursor.getString(EgoEaterUserTable.BIRTHDAY_COLUMN));
        }

        return IntegerUtil.get(age, NULL_AGE);
    }

    public static int getAge(UserBean userBean) {
        Integer age = getAge(userBean.getBirthdayOverride());

        if (age == null) {
            age = getAge(userBean.getBirthday());
        }

        return IntegerUtil.get(age, NULL_AGE);
    }
}
