package com.playposse.egoeater.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A dialog that only shows when there is no connection. The dialog will disappear on its own
 * when the connection is restored.
 */
public final class WaitingForConnectionDialog {

    private WaitingForConnectionDialog() {}

    public static void showIfNecessary(Activity activity, int titleId, int messageId) {
        // check if the dialog is necessary.
        if (hasConnectivity(activity)) {
            return;
        }

        // Show the dialog.
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(titleId)
                .setMessage(messageId)
                .setCancelable(false)
                .show();

        // Register BroadcastReceiver to monitor the connectivity.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        WaitingForConnectivityBroadcastReceiver receiver =
                new WaitingForConnectivityBroadcastReceiver(activity, dialog);
        activity.registerReceiver(receiver, filter);
    }

    private static boolean hasConnectivity(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * A {@link BroadcastReceiver} that waits for the connectivity to return. When it does, the
     * alert dialog is closed.
     */
    private static class WaitingForConnectivityBroadcastReceiver extends BroadcastReceiver {

        private final Context context;
        private final AlertDialog dialog;

        private WaitingForConnectivityBroadcastReceiver(Context context, AlertDialog dialog) {
            this.context = context;
            this.dialog = dialog;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity =
                    intent.getBooleanExtra("noConnectivity", false);

            if (!noConnectivity) {
                dialog.dismiss();

                context.unregisterReceiver(this);
            }
        }
    }
}
