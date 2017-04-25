package com.playposse.egoeater.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.playposse.egoeater.R;

/**
 * Utility that shows a simple alert dialog.
 */
public final class SimpleAlertDialog {

    private SimpleAlertDialog() {}

    public static void show(Context context, int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(
                R.string.dialog_okay_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }
        );
        builder.show();
    }
}
