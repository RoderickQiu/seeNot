package com.scrisstudio.seenot;

import android.util.Log;

public class Utils {
    public static final String TAG = "SeeNot-AccessibilityService";

    //log
    public static void l(Object input) {
        if (input != null)
            Log.w(TAG, input.toString() + " " + System.currentTimeMillis());
        else Log.w(TAG, "NULL" + " " + System.currentTimeMillis());
    }

    //log-error
    public static void le(Object input) {
        if (input != null)
            Log.e(TAG, input.toString() + " " + System.currentTimeMillis());
        else Log.e(TAG, "NULL" + " " + System.currentTimeMillis());
    }
}
