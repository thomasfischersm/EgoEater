package com.playposse.egoeater.util;

import androidx.annotation.Nullable;
import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Helpful methods for dealing with strings.
 */
public class StringUtil {

    public static boolean isEmpty(@Nullable String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static int countOccurrencesOf(String str, char c) {
        if (isEmpty(str)) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static String concat(List<String> strs, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    @javax.annotation.Nullable
    public static String getCleanString(TextView textView) {
        return getCleanString(textView.getText().toString());
    }

    @javax.annotation.Nullable
    public static String getCleanString(EditText editText) {
        return getCleanString(editText.getText());
    }

    @javax.annotation.Nullable
    public static String getCleanString(Editable editable) {
        return getCleanString(editable.toString());
    }

    @javax.annotation.Nullable
    public static String getCleanString(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            return (str.length() > 0) ? str : null;
        }
    }

    @javax.annotation.Nullable
    public static String trim(String str) {
        return (str != null) ? str.trim() : null;
    }
}
