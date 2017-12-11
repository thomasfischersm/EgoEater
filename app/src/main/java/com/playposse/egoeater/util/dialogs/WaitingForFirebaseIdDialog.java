package com.playposse.egoeater.util.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;

import com.google.firebase.iid.FirebaseInstanceId;
import com.playposse.egoeater.util.StringUtil;

/**
 * A dialog that waits for the Firebase id to be ready. A small number of users crashes because the
 * Firebase id is null. Give the Firebase service time to get the id. Plus, tell users to ensure
 * that the connectivity is there.
 */
public final class WaitingForFirebaseIdDialog {

    private static final int POLLING_INTERVAL = 1_000;

    private WaitingForFirebaseIdDialog() {}

    public static CheckFirebaseIdRunnable showIfNecessary(
            Activity activity,
            int titleId,
            int messageId,
            Runnable callback) {

        if (hasFirebaseId()) {
            // No need to show the dialog.
            callback.run();
            return null;
        }

        // Show the dialog.
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(titleId)
                .setMessage(messageId)
                .setCancelable(false)
                .show();

        // Add polling action to check when the id appears.
        Handler handler = new Handler();
        CheckFirebaseIdRunnable workerRunnable = new
                CheckFirebaseIdRunnable(handler, dialog, callback);
        handler.postDelayed(workerRunnable, POLLING_INTERVAL);
        return workerRunnable;
    }

    private static boolean hasFirebaseId() {
        return !StringUtil.isEmpty(FirebaseInstanceId.getInstance().getToken());
    }

    /**
     * A {@link Runnable} that checks if the Firebase id is available every second.
     */
    public static class CheckFirebaseIdRunnable implements Runnable {

        private final Handler handler;
        private final AlertDialog dialog;
        private final Runnable callback;

        private boolean isClosed = false;

        private CheckFirebaseIdRunnable(Handler handler, AlertDialog dialog, Runnable callback) {
            this.handler = handler;
            this.dialog = dialog;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (isClosed) {
                // Do nothing. This is the last execution before it ends.
            } else if (hasFirebaseId()) {
                dialog.dismiss();
                callback.run();
            } else {
                // Try again in a second.
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        }

        public void close() {
            isClosed = true;
        }

        public void restart() {
            isClosed = false;

            handler.post(this);
        }
    }
}
