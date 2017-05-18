package com.playposse.egoeater.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.playposse.egoeater.R;

/**
 * Utility that shows a simple alert dialog.
 */
public final class SimpleAlertDialog {

    private static final String LOG_TAG = SimpleAlertDialog.class.getSimpleName();

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

    /**
     * Shows a dialog that lets the user decide to pick or delete a photo.
     */
    public static void confirmPhoto(
            Context context,
            final Runnable pickPhotoRunnable,
            final Runnable deletePhotoRunnable) {

        new AlertDialog.Builder(context)
                .setTitle(R.string.photo_picker_dialog_title)
                .setItems(
                        R.array.photo_picker_dialog_items,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                switch (which) {
                                    case 0:
                                        pickPhotoRunnable.run();
                                        break;
                                    case 1:
                                        deletePhotoRunnable.run();
                                        break;
                                    default:
                                        Log.i(LOG_TAG, "onClick: Unexpected item was clicked: "
                                                + which);
                                        break;
                                }
                            }
                        })
                .setNegativeButton(
                        R.string.cancel_button_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();

    }
}
