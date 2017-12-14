package com.playposse.egoeater.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.playposse.egoeater.EgoEaterApplication;
import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.activity.CurrentActivity;
import com.playposse.egoeater.activity.NoConnectivityActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * A {@link BroadcastReceiver} that observes the network state. If there is no network connectivity,
 * the user will be directed to a warning page to suspend any possible network activity caused by
 * the user using the app.
 */
public class NetworkConnectivityBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EgoEaterApplication application = (EgoEaterApplication) context.getApplicationContext();

        boolean noConnectivity = intent.getBooleanExtra("noConnectivity", false);
        Class<? extends Activity> currentActivity = CurrentActivity.getCurrentActivity();
        boolean isOnNoConnectivityActivity = NoConnectivityActivity.class.equals(currentActivity);

        if (noConnectivity && (currentActivity != null) && !isOnNoConnectivityActivity) {
            Intent newActivityIntent = new Intent(context, NoConnectivityActivity.class);
            newActivityIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newActivityIntent);
            AnalyticsUtil.reportConnectivityLost(application);
        } else if (isOnNoConnectivityActivity && !noConnectivity) {
            GlobalRouting.onNetworkAvailable(context);
            AnalyticsUtil.reportConnectivityRestored(application);
        }
    }
}
