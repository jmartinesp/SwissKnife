package com.arasthel.swissknife.utils

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import groovy.transform.CompileStatic;

/**
 * Created by Arasthel on 16/08/14.
 */
@CompileStatic
class Finder {

    static final String TAG = "Finder";
    static final String ERROR_MSG = "Passed target is null, couldn't inject from it";

    static View findView(View target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG);
            return null;
        }

        int identifier = getViewId(target.context, idStr, type)
        return target.findViewById(identifier)
    }

    static View findView(Activity target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG);
            return null;
        }

        int identifier = getViewId(target, idStr, type)
        return target.findViewById(identifier)
    }

    private static int getViewId(Context context, String idStr, String type = null) {
        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)

        int identifier = resources.getIdentifier(idStr, "id", packageName)

        return identifier
    }

    private static Resources getResources(Context context, String type) {
        if (type?.startsWith("android.R\$")) {
            return Resources.getSystem()
        } else {
            return context.getResources()
        }
    }

    private static String getPackageName(Context context, String type) {
        if (type?.startsWith("android.R\$")) {
            return "android"
        } else {
            return context.getPackageName()
        }
    }

    static String getString(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "string", packageName)
        return resources.getString(identifier)
    }

    static Float getDimen(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "dimen", packageName)
        return resources.getDimension(identifier)
    }

    static Boolean getBoolean(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "bool", packageName)
        return resources.getBoolean(identifier)
    }

    static Integer getColor(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "color", packageName)
        return resources.getColor(identifier)
    }

    static Integer getInteger(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "integer", packageName)
        return resources.getInteger(identifier)
    }

    static int[] getIntegerArray(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "array", packageName)
        return resources.getIntArray(identifier)
    }

    static String[] getStringArray(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "array", packageName)
        return resources.getStringArray(identifier)
    }

    static Drawable getDrawable(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "drawable", packageName)

        int api = android.os.Build.VERSION.SDK_INT

        return api >= 21 ? resources.getDrawable(identifier, context.getTheme()) : resources.getDrawable(identifier)
    }

    static Animation getAnimation(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)

        int identifier = resources.getIdentifier(idStr, "anim", packageName)
        return AnimationUtils.loadAnimation(context, identifier)
    }

    static ColorStateList getColorStateList(Object target, String idStr, String type = null) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = getResources(context, type)
        String packageName = getPackageName(context, type)
        int identifier = resources.getIdentifier(idStr, "color", packageName)
        return resources.getColorStateList(identifier)
    }
}
