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

    public static View findView(Activity context, String idStr) {
        int identifier = context.getResources().getIdentifier(idStr, "id", context.packageName);
        View v = context.findViewById(identifier);
        return v;
    }

    public static View findView(View parentView, int id) {
        return parentView.findViewById(id);
    }
}
