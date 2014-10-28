package com.arasthel.swissknife.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import groovy.transform.CompileStatic;/**
 * Created by Arasthel on 16/08/14.
 */
@CompileStatic
public class Finder {

    public static final String TAG = "Finder";

    public static View findView(Activity context, String idStr) {
        int identifier = context.getResources().getIdentifier(idStr, "id", context.packageName);
        View v = context.findViewById(identifier);
        return v;
    }

    public static View findView(View parentView, String idStr) {
        if(!parentView) {
            Log.e(TAG, "Passed view is null, couldn't inject views from it");
            return null;
        }
        Context context = parentView.getContext();
        int identifier = context.getResources().getIdentifier(idStr, "id", context.packageName);
        View v = parentView.findViewById(identifier);
        return v;
    }
}
