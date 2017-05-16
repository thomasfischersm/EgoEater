package com.playposse.egoeater.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.playposse.egoeater.R;

/**
 * Utility that shows a simple alert dialog.
 */
public final class SimpleAlertDialog {

    private SimpleAlertDialog() {
    }

    /**
     * Shows a dialog with a message that the user can click "OK" on.
     */
    public static void alert(Context context, int titleId, int messageId) {
        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(
                        R.string.dialog_okay_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }

    /**
     * Shows a dialog with a message that the user can agree with or reject.
     */
    public static void confirm(
            Context context,
            int titleId,
            int messageId,
            final Runnable confirmationRunnable) {

        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(messageId)
                .setNegativeButton(
                        R.string.dialog_cancel_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                )
                .setPositiveButton(
                        R.string.dialog_continue_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                confirmationRunnable.run();
                            }
                        }
                )
                .show();
    }

    /**
     * Shows a dialog that asks to choose between cancel and discard.
     */
    public static void confirmDiscard(Context context, final Runnable discardRunnable) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.discard_dialog_title)
                .setMessage(R.string.discard_dialog_message)
                .setNegativeButton(
                        R.string.discard_dialog_cancel_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                )
                .setPositiveButton(
                        R.string.discard_dialog_discard_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                discardRunnable.run();
                            }
                        }
                )
                .show();
    }
}
