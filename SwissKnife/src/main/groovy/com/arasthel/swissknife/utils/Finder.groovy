package com.arasthel.swissknife.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import groovy.transform.CompileStatic;

/**
 * Created by Arasthel on 16/08/14.
 */
@CompileStatic
public class Finder {

    public static final String TAG = "Finder";

    public static View findView(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, "Passed target is null, couldn't inject views from it");
            return null;
        }

        if(target instanceof View) {
            View view = target as View
            Context context = view.getContext();
            int identifier = context.getResources().getIdentifier(idStr, "id", context.packageName);
            View v = target.findViewById(identifier);
            return v;
        } else {
            Activity activity = target as Activity
            int identifier = activity.getResources().getIdentifier(idStr, "id", activity.packageName);
            View v = activity.findViewById(identifier);
            return v;
        }


    }
}
