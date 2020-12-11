package com.shaubert.ui.imagepicker;

import android.util.Log;

import androidx.annotation.Nullable;

class DebugLog {

    public static final String TAG = DebugLog.class.getSimpleName();

    public static boolean ENABLED = false;

    public static void logError(String message, @Nullable Exception ex) {
        if (!ENABLED) return;

        if (ex != null) {
            Log.e(TAG, message, ex);
        } else {
            Log.e(TAG, message);
        }
    }

}
