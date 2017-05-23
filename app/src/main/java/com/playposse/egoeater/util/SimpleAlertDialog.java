package com.playposse.egoeater.util;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.playposse.egoeater.R;
import com.playposse.egoeater.clientactions.ReportAbuseClientAction;

import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.ratingEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reportAbuseEvent;

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

    public static void showReportAbuseDialog(
            final Context context,
            final long abuserId,
            final Application application) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(builder.getContext());
        editText.setHint(R.string.report_abuse_dialog_note_hint);
        editText.setMaxLines(5);
        editText.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(layoutParams);

        final AlertDialog dialog = builder
                .setTitle(R.string.report_abuse_dialog_title)
//                .setMessage(messageId)
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
                        R.string.report_abuse_dialog_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String note = editText.getText().toString();
                                if (StringUtil.isEmpty(note)) {
                                    return;
                                }

                                dialog.dismiss();

                                ReportAbuseClientAction reportAbuseClientAction =
                                        new ReportAbuseClientAction(
                                                context,
                                                abuserId,
                                                note);
                                reportAbuseClientAction.execute();

                                AnalyticsUtil.reportEvent(application, reportAbuseEvent, "");
                            }
                        }
                )
                .setView(editText)
                .create();

        // Enable the positive button only when the user has entered text.
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dlg) {
                final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(false);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Ignore
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Ignore
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String note = editText.getText().toString();
                        positiveButton.setEnabled(!StringUtil.isEmpty(note));
                    }
                });
            }
        });

        dialog.show();
    }
}
