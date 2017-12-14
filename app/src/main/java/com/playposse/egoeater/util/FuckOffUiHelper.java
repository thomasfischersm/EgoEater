package com.playposse.egoeater.util;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.MatchesActivity;
import com.playposse.egoeater.clientactions.FuckOffClientAction;
import com.playposse.egoeater.contentprovider.FuckOffUtil;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.dialogs.SimpleAlertDialog;

import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.fuckOffEvent;

/**
 * A class to share the functionality of the fuck off action.
 */
public class FuckOffUiHelper {

    public static void fuckOff(
            final Context context,
            final long partnerId,
            final Application application) {

        SimpleAlertDialog.confirm(
                context,
                R.string.fuck_off_dialog_title,
                R.string.fuck_off_dialog_text,
                new Runnable() {
                    @Override
                    public void run() {
                        fuckOffConfirmed(context, partnerId, application);
                    }
                }
        );
    }

    private static void fuckOffConfirmed(Context context, long partnerId, Application application) {
        ContentResolver contentResolver = context.getContentResolver();
        EgoEaterPreferences.addFuckOffUser(context, partnerId);
        new FuckOffClientAction(context, partnerId).execute();
        FuckOffUtil.eraseUserLocally(contentResolver, partnerId);

        context.startActivity(new Intent(context, MatchesActivity.class));

        Long userId = EgoEaterPreferences.getUser(context).getUserId();
        AnalyticsUtil.reportFuckOffEvent(application, userId, partnerId);
    }
}
